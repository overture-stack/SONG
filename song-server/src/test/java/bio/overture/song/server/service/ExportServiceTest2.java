package bio.overture.song.server.service;

import bio.overture.song.core.model.ExportedPayload;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.service.export.ExportService;
import bio.overture.song.server.utils.generator.StudyGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.CollectionUtils.mapToImmutableSet;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ExportServiceTest2 {

  @Autowired private StudyService studyService;
  @Autowired private UploadService uploadService;
  @Autowired private ExportService exportService;

  private StudyGenerator studyGenerator;
  private final RandomGenerator randomGenerator =
      createRandomGenerator(ExportServiceTest2.class.getSimpleName());

  @Before
  public void beforeTest() {
    studyGenerator = createStudyGenerator(studyService, randomGenerator);
  }

  @SneakyThrows
  private static String fromStatus(ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/" + key).asText("");
    return uploadId;
  }

  @Test
  public void testSingleExportWithAnalysisId() {
    runSingleExportTest(true);
  }

  @Test
  public void testSingleExportWithoutAnalysisId(){
    runSingleExportTest(false);
  }


  @Data
  @Accessors(chain = true)
  public static class TData {
    private String institution;
    private String studyId;
    private JsonNode inputPayload;
    private JsonNode expectedExportedPayload;
    private String analysisId;
  }

  private void runSingleExportTest(boolean includeAnalysisIds){
    // Generate data and look up for later
    val testData12 = generateTestData(new int[]{1,2}, includeAnalysisIds);
    val testData34 = generateTestData(new int[]{3,4}, includeAnalysisIds);
    val testLookup = Stream.of(testData12, testData34)
        .flatMap(Collection::stream)
        .collect(toMap(TData::getInstitution, identity()));


    //  Collect the set of studies
    val testData12_studyId = testData12.get(0).getStudyId();
    val testData34_studyId = testData34.get(0).getStudyId();
    val allStudies = ImmutableSet.of(testData12_studyId, testData34_studyId);

    // Collect a set of analysisIds
    val analysisIds = Stream.of(testData12, testData34)
        .flatMap(Collection::stream)
        .map(TData::getAnalysisId)
        .collect(toList());

    // Export payload and assert there are 2 results with different studies
    val actualExportedPayloads = exportService.exportPayload(analysisIds, includeAnalysisIds);
    assertEquals(actualExportedPayloads.size(), 2);
    assertNotEquals(actualExportedPayloads.get(0).getStudyId(), actualExportedPayloads.get(1).getStudyId());
    assertEquals(allStudies, mapToImmutableSet(actualExportedPayloads, ExportedPayload::getStudyId));

    // For each result, assert expected matches actual
    for(val actualExportedPayload : actualExportedPayloads){
      val actualExportedPayloadsJson = actualExportedPayload.getPayloads();
      assertEquals(actualExportedPayloadsJson.size(), 2);

      for (val actualPayloadJson : actualExportedPayloadsJson){
        val inst = actualPayloadJson.get("institution").textValue();
        log.info("Asserting institution: {}", inst);
        val testData = testLookup.get(inst);
        assertJsonEquals(testData.getExpectedExportedPayload(), actualPayloadJson, when(IGNORING_ARRAY_ORDER));
      }
    }
  }

  private List<TData> generateTestData(int[] fixtureNumbers, boolean includeAnalysisIds){
    // Generate a random study
    val studyId = studyGenerator.createRandomStudy();
    val output = ImmutableList.<TData>builder();
    for (val fixtureNumber : fixtureNumbers){
      val inputFilename = format("documents/export/variantcall-input%d.json", fixtureNumber);
      val inputPayloadJson = (ObjectNode)getJsonNodeFromClasspath(inputFilename);
      inputPayloadJson.put("study", studyId);

      val uploadId = fromStatus(uploadService.upload(studyId, toJson(inputPayloadJson), false), "uploadId");
      val analysisId = fromStatus(uploadService.save(studyId, uploadId, true), "analysisId");

      val outputFilename = format("documents/export/variantcall-output%d.json", fixtureNumber);
      val outputExportedPayloadJson = (ObjectNode)getJsonNodeFromClasspath(outputFilename);
      outputExportedPayloadJson.put("study", studyId);
      if(includeAnalysisIds){
        outputExportedPayloadJson.put("analysisId", analysisId);
      }

      output.add(new TData()
          .setInstitution("inst123-"+fixtureNumber)
          .setExpectedExportedPayload(outputExportedPayloadJson)
          .setInputPayload(inputPayloadJson)
          .setStudyId(studyId)
          .setAnalysisId(analysisId));
    }
    return output.build();
  }


}
