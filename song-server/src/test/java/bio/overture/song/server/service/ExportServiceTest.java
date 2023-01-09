/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.server.service;

import static bio.overture.song.core.utils.CollectionUtils.mapToImmutableSet;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY_ID;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import bio.overture.song.core.model.ExportedPayload;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.utils.generator.StudyGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.transaction.Transactional;
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

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ExportServiceTest {

  @Autowired private StudyService studyService;
  @Autowired private SubmitService submitService;
  @Autowired private ExportService exportService;

  private StudyGenerator studyGenerator;
  private final RandomGenerator randomGenerator =
      createRandomGenerator(ExportServiceTest.class.getSimpleName());

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
  @Transactional
  public void testGroupExport() {
    runGroupExportTest();
  }

  @Test
  @Transactional
  public void testSingleExportWithAnalysisId() {
    runSingleExportTest(true);
  }

  @Test
  @Transactional
  public void testSingleExportWithoutAnalysisId() {
    runSingleExportTest(false);
  }

  private void runGroupExportTest() {
    // Generate data and look up for later
    val testData12 = generateTestData(new int[] {1, 2});
    val testData34 = generateTestData(new int[] {3, 4});
    val testLookup =
        Stream.of(testData12, testData34)
            .flatMap(Collection::stream)
            .collect(toMap(TData::getInstitution, identity()));

    //  Collect the set of studies
    val testData12_studyId = testData12.get(0).getStudyId();
    val testData34_studyId = testData34.get(0).getStudyId();
    val allStudies = ImmutableSet.of(testData12_studyId, testData34_studyId);

    for (val studyId : allStudies) {
      val actualExportedPayloads = exportService.exportPayloadsForStudy(studyId);
      assertEquals(actualExportedPayloads.size(), 1);
      val actualExportedPayload = actualExportedPayloads.get(0);
      assertEquals(studyId, actualExportedPayload.getStudyId());

      // For each result, assert expected matches actual
      val actualExportedPayloadsJson = actualExportedPayload.getPayloads();
      for (val actualPayloadJson : actualExportedPayloadsJson) {
        val inst = actualPayloadJson.get("institution").textValue();
        log.info("Asserting institution: {}", inst);
        val testData = testLookup.get(inst);
        assertJsonEquals(
            testData.getExpectedExportedPayload(), actualPayloadJson, when(IGNORING_ARRAY_ORDER));
      }
    }
  }

  private void runSingleExportTest(boolean includeAnalysisIds) {
    // Generate data and look up for later
    val testData12 = generateTestData(new int[] {1, 2});
    val testData34 = generateTestData(new int[] {3, 4});
    val testLookup =
        Stream.of(testData12, testData34)
            .flatMap(Collection::stream)
            .collect(toMap(TData::getInstitution, identity()));

    //  Collect the set of studies
    val testData12_studyId = testData12.get(0).getStudyId();
    val testData34_studyId = testData34.get(0).getStudyId();
    val allStudies = ImmutableSet.of(testData12_studyId, testData34_studyId);

    // Collect a set of analysisIds
    val analysisIds =
        Stream.of(testData12, testData34)
            .flatMap(Collection::stream)
            .map(TData::getAnalysisId)
            .collect(toList());

    // Export payload and assert there are 2 results with different studies
    val actualExportedPayloads = exportService.exportPayload(analysisIds);
    assertEquals(actualExportedPayloads.size(), 2);
    assertNotEquals(
        actualExportedPayloads.get(0).getStudyId(), actualExportedPayloads.get(1).getStudyId());
    assertEquals(
        allStudies, mapToImmutableSet(actualExportedPayloads, ExportedPayload::getStudyId));

    // For each result, assert expected matches actual
    for (val actualExportedPayload : actualExportedPayloads) {
      val actualExportedPayloadsJson = actualExportedPayload.getPayloads();
      assertEquals(actualExportedPayloadsJson.size(), 2);

      for (val actualPayloadJson : actualExportedPayloadsJson) {
        val inst = actualPayloadJson.get("institution").textValue();
        log.info("Asserting institution: {}", inst);
        val testData = testLookup.get(inst);
        assertJsonEquals(
            testData.getExpectedExportedPayload(), actualPayloadJson, when(IGNORING_ARRAY_ORDER));
      }
    }
  }

  private List<TData> generateTestData(int[] fixtureNumbers) {
    // Generate a random study
    val studyId = studyGenerator.createRandomStudy();
    val output = ImmutableList.<TData>builder();
    for (val fixtureNumber : fixtureNumbers) {
      val inputFilename = format("documents/export/variantcall-input%d.json", fixtureNumber);
      val inputPayloadJson = (ObjectNode) getJsonNodeFromClasspath(inputFilename);
      inputPayloadJson.put(STUDY_ID, studyId);

      val analysisId = submitService.submit(studyId, toJson(inputPayloadJson)).getAnalysisId();

      val outputFilename = format("documents/export/variantcall-output%d.json", fixtureNumber);
      val outputExportedPayloadJson = (ObjectNode) getJsonNodeFromClasspath(outputFilename);
      outputExportedPayloadJson.put(STUDY_ID, studyId);

      output.add(
          new TData()
              .setInstitution("inst123-" + fixtureNumber)
              .setExpectedExportedPayload(outputExportedPayloadJson)
              .setInputPayload(inputPayloadJson)
              .setStudyId(studyId)
              .setAnalysisId(analysisId));
    }
    return output.build();
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
}
