/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
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

import static bio.overture.song.core.testing.SongErrorAssertions.assertCollectionsMatchExactly;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.Reductions.groupUnique;
import static bio.overture.song.server.model.enums.UploadStates.resolveState;
import static bio.overture.song.server.utils.TestAnalysis.extractNode;
import static bio.overture.song.server.utils.TestFiles.DEFAULT_EMPTY_VALUE;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.analysis.Analysis2;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.repository.AnalysisRepository;
import bio.overture.song.server.repository.SampleSetRepository;
import bio.overture.song.server.repository.SequencingReadRepository;
import bio.overture.song.server.repository.VariantCallRepository;
import bio.overture.song.server.service.export.ExportService;
import bio.overture.song.server.utils.generator.LegacyAnalysisTypeName;
import bio.overture.song.server.utils.generator.StudyGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
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

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static bio.overture.song.core.testing.SongErrorAssertions.assertCollectionsMatchExactly;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.Reductions.groupUnique;
import static bio.overture.song.server.model.enums.UploadStates.resolveState;
import static bio.overture.song.server.utils.TestAnalysis.extractNode;
import static bio.overture.song.server.utils.TestFiles.DEFAULT_EMPTY_VALUE;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ExportServiceTest {
  private static final String ANALYSIS_ID = "analysisId";
  private static final int DEFAULT_NUM_STUDIES = 3;
  private static final int DEFAULT_NUM_ANALYSIS_PER_STUDY = 4;
  private static final String STATUS = "status";
  private static final String OK = "ok";
  private static final String UPLOAD_ID = "uploadId";
  private static final boolean DEFAULT_INCLUDE_OTHER_IDS = false;

  @Autowired private ExportService exportService;

  @Autowired private UploadService uploadService;

  @Autowired private AnalysisService analysisService;

  @Autowired private StudyService studyService;

  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private AnalysisInfoService analysisInfoService;
  @Autowired private SequencingReadInfoService sequencingReadInfoService;
  @Autowired private VariantCallInfoService variantCallInfoService;
  @Autowired private DonorService donorService;
  @Autowired private FileService fileService;
  @Autowired private SampleSetRepository sampleSetRepository;
  @Autowired private SequencingReadRepository sequencingReadRepository;
  @Autowired private VariantCallRepository variantCallRepository;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(ExportServiceTest.class.getSimpleName());
  private StudyGenerator studyGenerator;

  @Before
  public void beforeTest() {
    studyGenerator = createStudyGenerator(studyService, randomGenerator);
  }

  @Test
  @Transactional
  public void testFullLoop() {
    runFullLoopTest(
        LegacyAnalysisTypeName.SEQUENCING_READ,
        DEFAULT_NUM_STUDIES,
        DEFAULT_NUM_ANALYSIS_PER_STUDY);
    runFullLoopTest(
        LegacyAnalysisTypeName.VARIANT_CALL, DEFAULT_NUM_STUDIES, DEFAULT_NUM_ANALYSIS_PER_STUDY);
    assert (true);
  }

  @Test
  public void testGrouping() {
    runExportTest(
        LegacyAnalysisTypeName.SEQUENCING_READ,
        DEFAULT_NUM_STUDIES,
        DEFAULT_NUM_ANALYSIS_PER_STUDY);
    runExportTest(
        LegacyAnalysisTypeName.VARIANT_CALL, DEFAULT_NUM_STUDIES, DEFAULT_NUM_ANALYSIS_PER_STUDY);
    assert (true);
  }

  @Test
  public void testSingleExportWithoutAnalysisId() {
    runSingleExportTest(LegacyAnalysisTypeName.SEQUENCING_READ, false);
    runSingleExportTest(LegacyAnalysisTypeName.VARIANT_CALL, false);
    assert (true);
  }

  @Test
  public void testSingleExportWithAnalysisId() {
    runSingleExportTest(LegacyAnalysisTypeName.SEQUENCING_READ, true);
    runSingleExportTest(LegacyAnalysisTypeName.VARIANT_CALL, true);
    assert (true);
  }

  @Test
  @Transactional
  public void testPayloadFixture() {
    // Create new study
    val studyId = studyGenerator.createRandomStudy();

    // Load the payload fixture, and modify its study to match the generated one above
    val expectedPayloadNode =
        (ObjectNode) getJsonNodeFromClasspath("documents/variantcall-valid.json");
    expectedPayloadNode.put("study", studyId);
    val expectedPayloadString = JsonUtils.toJson(expectedPayloadNode);

    // Upload the payload
    val uploadId =
        fromStatus(uploadService.upload(studyId, expectedPayloadString, false), "uploadId");

    // Save the payload
    val analysisId = fromStatus(uploadService.save(studyId, uploadId, false), "analysisId");

    // Export the payload
    val exportedPayloadsForStudies = exportService.exportPayload(newArrayList(analysisId), false);

    // Verify the input (expected) payload matches the output (actual) payload
    assertEquals(exportedPayloadsForStudies.size(), 1);
    val exportedPayloadForStudy = exportedPayloadsForStudies.get(0);
    assertEquals(exportedPayloadForStudy.getStudyId(), studyId);
    assertEquals(exportedPayloadForStudy.getPayloads().size(), 1);
    val actualPayloadNode = exportedPayloadForStudy.getPayloads().get(0);
    assertJsonEquals(expectedPayloadNode, actualPayloadNode, when(IGNORING_ARRAY_ORDER));
  }

  private void runSingleExportTest(
      LegacyAnalysisTypeName legacyAnalysisTypeName, boolean includeAnalysisId) {
    val studyId = studyGenerator.createRandomStudy();
    val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
    val expectedAnalysis = analysisGenerator.createDefaultRandomAnalysis(legacyAnalysisTypeName);
    val analysisId = expectedAnalysis.getAnalysisId();
    massageAnalysisInplace(expectedAnalysis, includeAnalysisId, DEFAULT_INCLUDE_OTHER_IDS);

    val exportedPayloads = exportService.exportPayload(newArrayList(analysisId), includeAnalysisId);
    assertEquals(exportedPayloads.size(), 1);
    val exportedPayload = exportedPayloads.get(0);
    assertEquals(exportedPayload.getStudyId(), studyId);

    val analyses =
        exportedPayload.getPayloads().stream()
            .map(x -> fromJson(x, Analysis2.class))
            .collect(toImmutableList());
    assertEquals(analyses.size(), 1);
    val actualAnalysis = analyses.get(0);
    assertAnalysis(actualAnalysis, expectedAnalysis);
  }

  public void runExportTest(
      LegacyAnalysisTypeName legacyAnalysisTypeName, int numStudies, int numAnalysesPerStudy) {

    val includeAnalysisId = true;

    // Check config
    assertCorrectConfig(numStudies, numAnalysesPerStudy);

    // Generate data
    val expectedData =
        generateData(
            legacyAnalysisTypeName,
            numStudies,
            numAnalysesPerStudy,
            includeAnalysisId,
            DEFAULT_INCLUDE_OTHER_IDS);

    // Process StudyMode Data
    val actualStudyModeExportedPayloads =
        expectedData.keySet().stream()
            .map(s -> exportService.exportPayloadsForStudy(s, includeAnalysisId))
            .flatMap(Collection::stream)
            .collect(toImmutableList());
    assertEquals(actualStudyModeExportedPayloads.size(), numStudies);
    val actualStudyModeData = Maps.<String, List<Analysis2>>newHashMap();
    for (val exportedPayload : actualStudyModeExportedPayloads) {
      val studyId = exportedPayload.getStudyId();
      val analyses =
          exportedPayload.getPayloads().stream()
              .map(x -> fromJson(x, Analysis2.class))
              .collect(toImmutableList());
      actualStudyModeData.put(studyId, analyses);
    }

    // Process AnalysisMode Data
    val expectedAnalysisIds =
        expectedData.values().stream()
            .flatMap(Collection::stream)
            .map(Analysis2::getAnalysisId)
            .collect(toImmutableList());
    val actualAnalysisModeExportedPayloads =
        exportService.exportPayload(expectedAnalysisIds, includeAnalysisId);
    assertEquals(actualAnalysisModeExportedPayloads.size(), numStudies);
    val actualAnalysisModeData = Maps.<String, List<Analysis2>>newHashMap();
    for (val exportedPayload : actualAnalysisModeExportedPayloads) {
      val studyId = exportedPayload.getStudyId();
      val analyses =
          exportedPayload.getPayloads().stream()
              .map(x -> fromJson(x, Analysis2.class))
              .collect(toImmutableList());
      actualAnalysisModeData.put(studyId, analyses);
    }

    assertMatchingData(actualAnalysisModeData, expectedData);
    assertMatchingData(actualStudyModeData, expectedData);
  }

  /** Delete the analysis */
  private void deleteAnalysis(Analysis2 a) {
    deleteExperiment(a);
    sampleSetRepository.deleteAllBySampleSetPK_AnalysisId(a.getAnalysisId());
    analysisInfoService.delete(a.getAnalysisId());
    a.getSample().stream()
        .map(CompositeEntity::getDonor)
        .map(Donor::getDonorId)
        .forEach(x -> donorService.securedDelete(a.getStudy(), x));
    a.getFile().stream()
        .map(FileEntity::getObjectId)
        .forEach(x -> fileService.securedDelete(a.getStudy(), x));
    analysisRepository.deleteById(a.getAnalysisId());
  }

  private void deleteExperiment(Analysis2 a) {
    ((ObjectNode) a.getAnalysisData().getData()).remove("experiment");
  }

  /**
   * Given that 0 < numStudies < numAnalysesPerStudy, this test ensures that if the export service
   * is requested for ONE analysisId from EACH study, the response will return a list of size {@code
   * numStudies}, where each element (which is of type ExportedPayload) has exactly ONE payload
   * (since there is only one analysis per study). This is a 2 in 1 test: - it verifies the
   * conversion of an existing analysis to a payload is correct, by submitting the converted payload
   * and comparing it with the original analysis - it verifies the aggregation functionality of the
   * export service, when given analysisIds belonging to different studies
   */
  private void runFullLoopTest(
      LegacyAnalysisTypeName legacyAnalysisTypeName, int numStudies, int numAnalysesPerStudy) {
    val includeAnalysisId = true;
    // Check the right parameters for this test are set
    assertCorrectConfig(numStudies, numAnalysesPerStudy);

    // Generate studies and there associated analyses
    val data =
        generateData(
            legacyAnalysisTypeName, numStudies, numAnalysesPerStudy, includeAnalysisId, true);

    // [REDUCTION_TAG] Reduce the data so that there is one analysis for each study
    val reducedData = Maps.<String, Analysis2>newHashMap();
    data.entrySet().stream()
        .filter(e -> !reducedData.containsKey(e.getKey()))
        .forEach(
            e -> {
              String studyId = e.getKey();
              List<Analysis2> analyses = e.getValue();
              int numAnalyses = analyses.size();
              int randomAnalysisPos = randomGenerator.generateRandomIntRange(0, numAnalyses);
              Analysis2 randomAnalysis = analyses.get(randomAnalysisPos);
              reducedData.put(studyId, randomAnalysis);
            });
    assertEquals(reducedData.keySet().size(), numStudies);
    assertEquals(reducedData.values().size(), numStudies);

    // Create a list of analysisIds that covers all the previously generated studies
    val requestedAnalysisIds =
        reducedData.values().stream().map(Analysis2::getAnalysisId).collect(toImmutableList());
    assertEquals(requestedAnalysisIds.size(), numStudies);

    // Export the analysis for the requested analysisIds
    val exportedPayloads = exportService.exportPayload(requestedAnalysisIds, includeAnalysisId);

    // There should be an ExportedPayload object for each study
    assertEquals(exportedPayloads.size(), numStudies);

    for (val exportedPayload : exportedPayloads) {
      // There should be only 1 analysis for each study. Refer to the comment with REDUCTION_TAG.
      val studyId = exportedPayload.getStudyId();
      assertEquals(exportedPayload.getPayloads().size(), 1);
      val payload = exportedPayload.getPayloads().get(0);
      val expectedAnalysis = reducedData.get(studyId);

      // Delete the previously created analysis so it can be created using the uploadService
      // (frontdoor creation)
      deleteAnalysis(expectedAnalysis);

      // Depending on includeAnalysisId's value, either remove or keep the analysisId. Always keep
      // the
      // other ids, so that it can be compared to the re-create analysis after submission
      massageAnalysisInplace(expectedAnalysis, includeAnalysisId, true);

      // Submit payload. Should create the same "otherIds" as the expected
      val actualAnalysis = submitPayload(studyId, payload);

      // Assert output analysis is correct
      assertAnalysis(actualAnalysis, expectedAnalysis);
    }
  }

  private Analysis2 submitPayload(String studyId, JsonNode payloadJson) {
    // Upload and check if successful
    val payload = toJson(payloadJson);
    val uploadStatus = uploadService.upload(studyId, payload, false);
    val status1 = fromStatus(uploadStatus, STATUS);
    assertEquals(status1, OK);
    val uploadId = fromStatus(uploadStatus, UPLOAD_ID);

    // Check Status and check if validated
    val statusResponse = uploadService.securedRead(studyId, uploadId);
    val uploadState = resolveState(statusResponse.getState());
    assertEquals(uploadState, UploadStates.VALIDATED);

    // Save and check if successful
    val analysisResponse = uploadService.save(studyId, uploadId, true);
    val status2 = fromStatus(analysisResponse, STATUS);
    assertEquals(status2, OK);
    val analysisId = fromStatus(analysisResponse, ANALYSIS_ID);

    return analysisService.securedDeepRead(studyId, analysisId);
  }

  /**
   * Generate {@code numStudies} studies and for each study generate {@code numAnalysisPerStudy}
   * analyses, and put everything in a map, where the keys are studyIds and the values are all the
   * analyses for that study
   */
  private Map<String, List<Analysis2>> generateData(
      LegacyAnalysisTypeName legacyAnalysisTypeName,
      int numStudies,
      int numAnalysesPerStudy,
      boolean includeAnalysisId,
      boolean includeOtherIds) {

    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    val map = Maps.<String, List<Analysis2>>newHashMap();
    for (int s = 0; s < numStudies; s++) {
      val studyId = studyGenerator.createRandomStudy();
      val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
      val analyses =
          range(0, numAnalysesPerStudy)
              .boxed()
              .map(x -> analysisGenerator.createDefaultRandomAnalysis(legacyAnalysisTypeName))
              .peek(x -> massageAnalysisInplace(x, includeAnalysisId, includeOtherIds))
              .collect(toImmutableList());
      map.put(studyId, analyses);
    }
    return ImmutableMap.copyOf(map);
  }

  private static void assertAnalysis(Analysis2 actualAnalysis, Analysis2 expectedAnalysis) {
    assertEquals(actualAnalysis.getAnalysisSchema(), expectedAnalysis.getAnalysisSchema());

    assertEquals(actualAnalysis.getAnalysisState(), expectedAnalysis.getAnalysisState());
    assertEquals(actualAnalysis.getAnalysisData(), expectedAnalysis.getAnalysisData());

    assertEquals(actualAnalysis.getAnalysisId(), expectedAnalysis.getAnalysisId());

    // All of the actuals will have objectIds, sampleIds, specimenIds, donorIds, and analysisIds,
    // however
    assertExperiment(actualAnalysis, expectedAnalysis);
    assertTrue(actualAnalysis.getFile().containsAll(expectedAnalysis.getFile()));
    assertTrue(actualAnalysis.getSample().containsAll(expectedAnalysis.getSample()));
    assertEquals(actualAnalysis.getStudy(), expectedAnalysis.getStudy());

    val actualFileObjectIds = collectObjectIds(actualAnalysis.getFile());
    val expectedFileObjectIds = collectObjectIds(expectedAnalysis.getFile());
    assertCollectionsMatchExactly(actualFileObjectIds, expectedFileObjectIds);

    assertSubmitterIds(actualAnalysis, expectedAnalysis);
  }

  private static void assertExperiment(Analysis2 actual, Analysis2 expected) {
    val actualExperimentNode = extractNode(actual, "experiment");
    val expectedExperimentNode = extractNode(expected, "experiment");
    assertEquals(actualExperimentNode, expectedExperimentNode);
  }

  private static void assertSubmitterIds(Analysis2 actualAnalysis, Analysis2 expectedAnalysis) {
    val actualSampleIds = collectSampleSubmitterIds(actualAnalysis.getSample());
    val expectedSampleIds = collectSampleSubmitterIds(expectedAnalysis.getSample());
    assertCollectionsMatchExactly(actualSampleIds, expectedSampleIds);

    val actualSpecimenIds = collectSpecimenSubmitterIds(actualAnalysis.getSample());
    val expectedSpecimenIds = collectSpecimenSubmitterIds(expectedAnalysis.getSample());
    assertCollectionsMatchExactly(actualSpecimenIds, expectedSpecimenIds);

    val actualDonorIds = collectDonorSubmitterIds(actualAnalysis.getSample());
    val expectedDonorIds = collectDonorSubmitterIds(expectedAnalysis.getSample());
    assertCollectionsMatchExactly(actualDonorIds, expectedDonorIds);
  }

  private static Set<String> collectObjectIds(List<FileEntity> files) {
    return files.stream().map(FileEntity::getObjectId).collect(toSet());
  }

  private static Set<String> collectSampleSubmitterIds(List<? extends Sample> samples) {
    return samples.stream().map(Sample::getSampleSubmitterId).collect(toSet());
  }

  private static Set<String> collectSpecimenSubmitterIds(List<CompositeEntity> compositeEntities) {
    return compositeEntities.stream()
        .map(CompositeEntity::getSpecimen)
        .map(Specimen::getSpecimenSubmitterId)
        .collect(toSet());
  }

  private static Set<String> collectDonorSubmitterIds(List<CompositeEntity> compositeEntities) {
    return compositeEntities.stream()
        .map(CompositeEntity::getDonor)
        .map(Donor::getDonorSubmitterId)
        .collect(toSet());
  }

  @SneakyThrows
  private static String fromStatus(ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/" + key).asText("");
    return uploadId;
  }

  private static void massageAnalysisInplace(
      Analysis2 a, boolean includeAnalysisId, boolean includeOtherIds) {
    if (!includeAnalysisId) {
      a.setAnalysisId(DEFAULT_EMPTY_VALUE);
    }

    if (!includeOtherIds) {
      a.getFile()
          .forEach(
              x -> {
                x.setAnalysisId(DEFAULT_EMPTY_VALUE);
                x.setObjectId(DEFAULT_EMPTY_VALUE);
                x.setStudyId(DEFAULT_EMPTY_VALUE);
              });

      a.getSample()
          .forEach(
              x -> {
                x.setSampleId(DEFAULT_EMPTY_VALUE);
                x.setSpecimenId(DEFAULT_EMPTY_VALUE);
                x.getDonor().setDonorId(DEFAULT_EMPTY_VALUE);
                x.getDonor().setStudyId(DEFAULT_EMPTY_VALUE);
                x.getSpecimen().setDonorId(DEFAULT_EMPTY_VALUE);
                x.getSpecimen().setSpecimenId(DEFAULT_EMPTY_VALUE);
              });
    }
  }

  private static void assertCorrectConfig(int numStudies, int numAnalysesPerStudy) {
    assertTrue(numStudies < numAnalysesPerStudy);
    assertTrue(numStudies > 0);
  }

  private static void assertMatchingData(
      Map<String, List<Analysis2>> actualData, Map<String, List<Analysis2>> expectedData) {
    assertCollectionsMatchExactly(expectedData.keySet(), actualData.keySet());
    for (val studyId : expectedData.keySet()) {
      val expectedAnalyses = expectedData.get(studyId);
      val actualAnalysisMap =
          groupUnique(
              actualData.get(studyId),
              Analysis2::getAnalysisId); // Assumed that all analyses in the list are unique
      for (val expectedAnalysis : expectedAnalyses) {
        assertTrue(actualAnalysisMap.containsKey(expectedAnalysis.getAnalysisId()));
        val actualAnalysis = actualAnalysisMap.get(expectedAnalysis.getAnalysisId());
        assertAnalysis(actualAnalysis, expectedAnalysis);
      }
    }
  }
}
