package bio.overture.song.server.contract;

import static bio.overture.song.server.utils.contract.GoldenFiles.assertMatchesGolden;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.model.StorageObject;
import bio.overture.song.server.service.StorageService;
import bio.overture.song.server.utils.contract.JsonNormalizer;
import bio.overture.song.server.utils.contract.RawHttpClient;
import bio.overture.song.server.utils.contract.RawResponse;
import bio.overture.song.server.utils.contract.RecordingSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base for tests that lock the server's wire contract with golden files.
 *
 * <p>Requests go over real HTTP to a server on a random port, so the responses include everything a
 * client sees: Tomcat error dispatches, message converters and response headers. Score is replaced
 * by a mocked {@link StorageService}, and Kafka messages are captured by a {@link RecordingSender}.
 *
 * <p>Requests are not rolled back. Surefire starts a new JVM, and therefore a new database, for
 * each test class, so each subclass sets up its data once and can rely on the seeded {@code
 * data.sql} rows (study {@code ABC123}, analyses {@code AN1}/{@code AN2}, files {@code FI1}-{@code
 * FI4}).
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(AbstractContractTest.ContractTestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractContractTest {

  protected static final ObjectMapper MAPPER = new ObjectMapper();

  /** Name of the VCF file in {@code documents/contract/variantcall-payload.json}. */
  protected static final String VCF_FILE_NAME = "contract-sample.vcf.gz";

  private static final ResourceFetcher CONTRACT_DOCUMENTS =
      ResourceFetcher.builder()
          .resourceType(ResourceFetcher.ResourceType.TEST)
          .dataDir(Paths.get("documents/contract"))
          .build();

  @LocalServerPort private int port;

  @MockBean protected StorageService storageService;

  @Autowired protected RecordingSender recordingSender;

  protected String baseUrl;
  protected RawHttpClient client;

  @BeforeAll
  public void setUpClient() {
    this.baseUrl = "http://localhost:" + port;
    this.client = new RawHttpClient(baseUrl);
  }

  /** A normalizer that already replaces the server's base URL, which contains the random port. */
  protected JsonNormalizer createNormalizer() {
    return new JsonNormalizer().replaceValue(baseUrl, "baseUrl");
  }

  protected void assertGolden(
      @NonNull String goldenName,
      @NonNull RawResponse response,
      @NonNull JsonNormalizer normalizer) {
    assertMatchesGolden(goldenName, normalizer.normalize(response.toEnvelope()));
  }

  protected void createStudy(@NonNull String studyId) {
    val study = MAPPER.createObjectNode();
    study.put("studyId", studyId);
    study.put("name", "Contract study " + studyId);
    study.put("organization", "Contract Test Institute");
    study.put("description", "Study created by the contract tests");
    study.putObject("info").put("purpose", "contract");
    val response = client.post("/studies/" + studyId + "/", study.toString());
    assertEquals(200, response.getStatus(), response.getBody());
  }

  /** Reads the contract variantCall payload fixture with its studyId set to {@code studyId}. */
  protected ObjectNode readVariantCallPayload(@NonNull String studyId) {
    val payload = (ObjectNode) CONTRACT_DOCUMENTS.readJsonNode("variantcall-payload.json");
    payload.put("studyId", studyId);
    return payload;
  }

  /**
   * Submits a payload, allowing duplicates so the same fixture can be submitted more than once.
   *
   * @return the new analysisId; fails the test when the submission is rejected
   */
  protected String submit(@NonNull String studyId, @NonNull ObjectNode payload) {
    val response = client.post("/submit/" + studyId + "?allowDuplicates=true", payload.toString());
    assertEquals(200, response.getStatus(), response.getBody());
    return response.bodyAsJson().path("analysisId").asText();
  }

  /** Reads the files of an analysis and returns their objectIds keyed by file name. */
  protected Map<String, String> readObjectIdsByFileName(
      @NonNull String studyId, @NonNull String analysisId) {
    val response = client.get("/studies/" + studyId + "/analysis/" + analysisId + "/files");
    assertEquals(200, response.getStatus(), response.getBody());
    val objectIdsByFileName = new LinkedHashMap<String, String>();
    for (val file : response.bodyAsJson()) {
      objectIdsByFileName.put(file.path("fileName").asText(), file.path("objectId").asText());
    }
    return objectIdsByFileName;
  }

  /** Makes the mocked Score report every file of the analysis as uploaded with matching data. */
  protected void stubStorageWithUploadedFiles(@NonNull String studyId, @NonNull String analysisId) {
    val response = client.get("/studies/" + studyId + "/analysis/" + analysisId + "/files");
    assertEquals(200, response.getStatus(), response.getBody());
    for (val file : response.bodyAsJson()) {
      val objectId = file.path("objectId").asText();
      when(storageService.isObjectExist(objectId)).thenReturn(true);
      when(storageService.downloadObject(objectId))
          .thenReturn(
              StorageObject.builder()
                  .objectId(objectId)
                  .fileMd5sum(file.path("fileMd5sum").asText())
                  .fileSize(file.path("fileSize").asLong())
                  .build());
    }
  }

  protected void publish(@NonNull String studyId, @NonNull String analysisId) {
    stubStorageWithUploadedFiles(studyId, analysisId);
    val response =
        client.send("PUT", "/studies/" + studyId + "/analysis/publish/" + analysisId, null, null);
    assertEquals(200, response.getStatus(), response.getBody());
  }

  /**
   * Registers the ids of an analysis as named placeholders, {@code <analysisId:LABEL>} and {@code
   * <objectId:LABEL:FILE_NAME>}, and sorts {@code files} arrays by file name because the server
   * returns an analysis's files in no fixed order.
   */
  protected JsonNormalizer withAnalysisIds(
      @NonNull JsonNormalizer normalizer,
      @NonNull String label,
      @NonNull String studyId,
      @NonNull String analysisId) {
    normalizer.replaceValue(analysisId, "analysisId:" + label).sortArray("files", "fileName");
    readObjectIdsByFileName(studyId, analysisId)
        .forEach(
            (fileName, objectId) ->
                normalizer.replaceValue(objectId, "objectId:" + label + ":" + fileName));
    return normalizer;
  }

  /** The objectId of the fixture's VCF file in an analysis created from the contract payload. */
  protected String readVcfObjectId(@NonNull String studyId, @NonNull String analysisId) {
    return readObjectIdsByFileName(studyId, analysisId).get(VCF_FILE_NAME);
  }

  @TestConfiguration
  static class ContractTestConfig {
    @Bean
    @Primary
    RecordingSender recordingSender() {
      return new RecordingSender();
    }
  }
}
