package bio.overture.song.server.repository;

import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.entity.InfoPK.createInfoPK;
import static bio.overture.song.server.utils.contract.GoldenFiles.assertMatchesGolden;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.overture.song.core.model.AnalysisTypeOptions;
import bio.overture.song.core.model.ExternalValidation;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.model.entity.Info;
import bio.overture.song.server.service.InfoService.FileInfoService;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.analysis.AnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Locks how each JSON column is written and read back, with golden files under {@code golden/db}.
 *
 * <p>The columns are mapped with hibernate-native-json ({@code CustomJsonType}), which will be
 * replaced. Each golden file holds the value read back through JPA ({@code readBack}) and the text
 * Postgres stores ({@code storedText}). The input has unordered keys, nested arrays, unicode, large
 * and fractional numbers, null, an empty string and empty containers.
 *
 * <p>{@code info} is a {@code json} column, so its stored text keeps the key order and spacing that
 * the mapper wrote; the other columns are {@code jsonb}, which Postgres normalizes.
 */
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class JsonColumnRoundTripTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final String TRICKY_JSON =
      "{\"zeta\": 1, \"alpha\": {\"list\": [1, 2.5, \"three\", null, {\"deep\": true}]},"
          + " \"unicode\": \"données – 数据 😀\", \"emptyString\": \"\", \"nullValue\": null,"
          + " \"bigNumber\": 12345678901234567890, \"fraction\": -0.001, \"flag\": false,"
          + " \"emptyObject\": {}, \"emptyArray\": []}";

  @Autowired private EntityManager entityManager;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private InfoRepository infoRepository;
  @Autowired private AnalysisSchemaRepository analysisSchemaRepository;
  @Autowired private AnalysisDataRepository analysisDataRepository;
  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private UpgradedAnalysisRepository upgradedAnalysisRepository;
  @Autowired private FileInfoService fileInfoService;
  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;

  private String studyId;

  @BeforeEach
  public void beforeEachTest() {
    this.studyId =
        createStudyGenerator(studyService, createRandomGenerator(getClass().getSimpleName()))
            .createRandomStudy();
  }

  @SneakyThrows
  private static ObjectNode trickyNode() {
    return (ObjectNode) MAPPER.readTree(TRICKY_JSON);
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private static Map<String, Object> trickyMap() {
    return MAPPER.readValue(TRICKY_JSON, Map.class);
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private static void assertRoundTrip(String goldenName, Object readBack, String storedText) {
    val document = MAPPER.createObjectNode();
    document.set("readBack", MAPPER.valueToTree(readBack));
    document.put("storedText", storedText);
    assertMatchesGolden("db/" + goldenName, document);
  }

  private static AnalysisTypeOptions trickyOptions() {
    return AnalysisTypeOptions.builder()
        .fileTypes(List.of("BAM", "VCF"))
        .externalValidations(
            List.of(
                ExternalValidation.builder()
                    .jsonPath("experiment.donorId")
                    .url("https://example.org/validate/{value}")
                    .build()))
        .build();
  }

  @Test
  public void info_throughRepository() {
    val infoPK = createInfoPK("RT-INFO-REPOSITORY", "File");
    infoRepository.save(Info.createInfo(infoPK, trickyMap()));
    flushAndClear();

    val readBack = infoRepository.findById(infoPK).get().getInfo();
    val storedText =
        jdbcTemplate.queryForObject(
            "select info::text from info where id = ? and id_type = 'File'",
            String.class,
            infoPK.getId());
    assertRoundTrip("info-repository", readBack, storedText);
  }

  /** The path used when files and analyses are submitted: the info arrives as a JSON string. */
  @Test
  public void info_throughInfoService() {
    fileInfoService.create("RT-INFO-SERVICE", TRICKY_JSON);
    flushAndClear();

    val readBack = fileInfoService.readInfo("RT-INFO-SERVICE").orElse(null);
    val storedText =
        jdbcTemplate.queryForObject(
            "select info::text from info where id = ? and id_type = 'File'",
            String.class,
            "RT-INFO-SERVICE");
    assertRoundTrip("info-service", readBack, storedText);
  }

  @Test
  public void analysisSchema_schemaAndOptions() {
    val saved =
        analysisSchemaRepository.save(
            AnalysisSchema.builder()
                .name("roundTripType")
                .version(1)
                .schema(trickyNode())
                .options(trickyOptions())
                .build());
    flushAndClear();

    val readBack = analysisSchemaRepository.findById(saved.getId()).get();
    val storedText =
        jdbcTemplate.queryForObject(
            "select json_build_object('schema', schema, 'options', options)::text"
                + " from analysis_schema where id = ?",
            String.class,
            saved.getId());
    assertRoundTrip(
        "analysis-schema",
        Map.of("schema", readBack.getSchema(), "options", readBack.getOptions()),
        storedText);
  }

  /**
   * Creates an analysis whose dynamic data and first file info contain the tricky value, then
   * returns its id.
   */
  private String createTrickyAnalysis() {
    val payload = MAPPER.createObjectNode();
    payload.put("studyId", studyId);
    payload.putObject("analysisType").put("name", "variantCall").put("version", 1);
    payload.set("tricky", trickyNode());
    val file = payload.putArray("files").addObject();
    file.put("fileName", "round-trip.vcf.gz");
    file.put("fileSize", 10);
    file.put("fileType", "VCF");
    file.put("fileMd5sum", "61a91c4bf04ac2bd3795254104f75ad3");
    file.put("fileAccess", "open");
    file.put("dataType", "SOME_DATA_TYPE");
    file.set("info", trickyNode());
    val analysisId =
        analysisService.create(studyId, fromJson(payload, Payload.class)).getAnalysisId();
    flushAndClear();
    return analysisId;
  }

  @Test
  public void analysisData_data() {
    val analysisId = createTrickyAnalysis();
    val analysisDataId = analysisRepository.findById(analysisId).get().getAnalysisData().getId();
    flushAndClear();

    val readBack = analysisDataRepository.findById(analysisDataId).get().getData();
    val storedText =
        jdbcTemplate.queryForObject(
            "select data::text from analysis_data where id = ?", String.class, analysisDataId);
    assertRoundTrip("analysis-data", readBack, storedText);
  }

  @Test
  public void analysisDataJoin_data() {
    createTrickyAnalysis();
    val joins =
        upgradedAnalysisRepository.getAnalysisData(studyId, Set.of(UNPUBLISHED.name()), 10, 0);
    assertEquals(1, joins.size());
    assertMatchesGolden("db/analysis-data-join", MAPPER.valueToTree(joins.get(0).getData()));
  }

  @Test
  public void analysisSchemaJoin_schemaAndOptions() {
    val analysisId = createTrickyAnalysis();
    val analysis = analysisRepository.findById(analysisId).get();
    val schema = analysis.getAnalysisSchema();
    schema.setSchema(trickyNode());
    schema.setOptions(trickyOptions());
    flushAndClear();

    val joins =
        upgradedAnalysisRepository.getAnalysisSchema(studyId, Set.of(UNPUBLISHED.name()), 10, 0);
    assertEquals(1, joins.size());
    final JsonNode readBack =
        MAPPER.valueToTree(
            Map.of("schema", joins.get(0).getSchema(), "options", joins.get(0).getOptions()));
    assertMatchesGolden("db/analysis-schema-join", readBack);
  }

  @Test
  public void dataEntity_fileInfo() {
    createTrickyAnalysis();
    val dataEntities =
        upgradedAnalysisRepository.getAnalysisFromDB(studyId, Set.of(UNPUBLISHED.name()), 10, 0);
    assertEquals(1, dataEntities.size());
    assertMatchesGolden(
        "db/data-entity-file-info", MAPPER.valueToTree(dataEntities.get(0).getFileInfo()));
  }
}
