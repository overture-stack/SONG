package org.icgc.dcc.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.model.enums.UploadStates;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.service.export.ExportService;
import org.icgc.dcc.song.server.utils.StudyGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.core.utils.Reductions.groupUnique;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.resolveAnalysisType;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;
import static org.icgc.dcc.song.server.utils.AnalysisGenerator.createAnalysisGenerator;
import static org.icgc.dcc.song.server.utils.StudyGenerator.createStudyGenerator;
import static org.icgc.dcc.song.server.utils.TestFiles.EMPTY_STRING;
import static org.icgc.dcc.song.server.utils.TestFiles.assertSetsMatch;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class ExportServiceTest {
  private static final String ANALYSIS_ID = "analysisId";
  private static final int DEFAULT_NUM_STUDIES = 3;
  private static final int DEFAULT_NUM_ANALYSIS_PER_STUDY= 4;
  private static final String STATUS = "status";
  private static final String OK = "ok";
  private static final String UPLOAD_ID = "uploadId";

  @Autowired
  private ExportService exportService;

  @Autowired
  private UploadService uploadService;

  @Autowired
  private AnalysisService analysisService;

  @Autowired
  private StudyService studyService;

  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private AnalysisInfoService analysisInfoService;
  @Autowired private SequencingReadInfoService sequencingReadInfoService;
  @Autowired private VariantCallInfoService variantCallInfoService;
  @Autowired private DonorService donorService;
  @Autowired private FileService fileService;

  private final RandomGenerator randomGenerator = createRandomGenerator(ExportServiceTest.class.getSimpleName());
  private StudyGenerator studyGenerator;

  @Before
  public void beforeTest(){
    studyGenerator = createStudyGenerator(studyService, randomGenerator);
  }

  @Test
  public void testFullLoop(){
    runFullLoopTest(SequencingReadAnalysis.class, DEFAULT_NUM_STUDIES, DEFAULT_NUM_ANALYSIS_PER_STUDY);
    runFullLoopTest(VariantCallAnalysis.class, DEFAULT_NUM_STUDIES, DEFAULT_NUM_ANALYSIS_PER_STUDY);
    assert(true);
  }

  @Test
  public void testGrouping(){
    runExportTest(SequencingReadAnalysis.class, DEFAULT_NUM_STUDIES, DEFAULT_NUM_ANALYSIS_PER_STUDY);
    runExportTest(VariantCallAnalysis.class, DEFAULT_NUM_STUDIES, DEFAULT_NUM_ANALYSIS_PER_STUDY );
    assert(true);
  }

  @Test
  public void testSingleExport00(){
    runSingleExportTest(SequencingReadAnalysis.class, false, false);
    runSingleExportTest(VariantCallAnalysis.class, false, false);
    assert(true);
  }

  @Test
  public void testSingleExport01(){
    runSingleExportTest(SequencingReadAnalysis.class, false, true);
    runSingleExportTest(VariantCallAnalysis.class, false, true);
    assert(true);
  }

  @Test
  public void testSingleExport10(){
    runSingleExportTest(SequencingReadAnalysis.class, true, false);
    runSingleExportTest(VariantCallAnalysis.class, true, false);
    assert(true);
  }

  @Test
  public void testSingleExport11(){
    runSingleExportTest(SequencingReadAnalysis.class, true, true);
    runSingleExportTest(VariantCallAnalysis.class, true, true);
    assert(true);
  }

  private void runSingleExportTest(Class<? extends Analysis> analysisClass,
      boolean includeAnalysisId, boolean includeOtherIds){
    val studyId = studyGenerator.createRandomStudy();
    val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
    val expectedAnalysis = analysisGenerator.createDefaultRandomAnalysis(analysisClass);
    val analysisId = expectedAnalysis.getAnalysisId();
    massageAnalysisInplace(expectedAnalysis, includeAnalysisId, includeOtherIds);

    val exportedPayloads = exportService.exportPayload(newArrayList(analysisId), includeAnalysisId, includeOtherIds);
    assertThat(exportedPayloads).hasSize(1);
    val exportedPayload = exportedPayloads.get(0);
    assertThat(exportedPayload.getStudyId()).isEqualTo(studyId);

    val analyses = exportedPayload.getPayloads().stream()
        .map(x -> fromJson(x, Analysis.class))
        .collect(toImmutableList());
    assertThat(analyses).hasSize(1);
    val actualAnalysis = analyses.get(0);
    assertAnalysis(actualAnalysis, expectedAnalysis);
  }

  public void runExportTest(Class<? extends Analysis> analysisClass,
      int numStudies, int numAnalysesPerStudy){

    val includeAnalysisId = true;
    val includeOtherIds = false;

    // Check config
    assertCorrectConfig(numStudies, numAnalysesPerStudy);

    // Generate data
    val expectedData = generateData(analysisClass, numStudies, numAnalysesPerStudy, includeAnalysisId, includeOtherIds);


    // Process StudyMode Data
    val actualStudyModeExportedPayloads = expectedData.keySet().stream()
        .map(s -> exportService.exportPayloadsForStudy(s, includeAnalysisId, includeOtherIds))
        .flatMap(Collection::stream)
        .collect(toImmutableList());
    assertThat(actualStudyModeExportedPayloads).hasSize(numStudies);
    val actualStudyModeData = Maps.<String, List<? extends Analysis>>newHashMap();
    for (val exportedPayload : actualStudyModeExportedPayloads){
      val studyId = exportedPayload.getStudyId();
      val analyses = exportedPayload.getPayloads().stream()
          .map(x -> fromJson(x, Analysis.class))
          .collect(toImmutableList());
      actualStudyModeData.put(studyId, analyses);
    }

    // Process AnalysisMode Data
    val expectedAnalysisIds =
        expectedData.values().stream()
            .flatMap(Collection::stream)
            .map(Analysis::getAnalysisId)
            .collect(toImmutableList());
    val actualAnalysisModeExportedPayloads =
        exportService.exportPayload(expectedAnalysisIds, includeAnalysisId, includeOtherIds);
    assertThat(actualAnalysisModeExportedPayloads).hasSize(numStudies);
    val actualAnalysisModeData = Maps.<String, List<? extends Analysis>>newHashMap();
    for (val exportedPayload : actualAnalysisModeExportedPayloads){
      val studyId = exportedPayload.getStudyId();
      val analyses = exportedPayload.getPayloads().stream()
          .map(x -> fromJson(x, Analysis.class))
          .collect(toImmutableList());
      actualAnalysisModeData.put(studyId, analyses);
    }

    assertMatchingData(actualAnalysisModeData, expectedData);
    assertMatchingData(actualStudyModeData, expectedData);
  }

  /**
   * Delete the analysis
   */
  private void deleteAnalysis(Analysis a){
    deleteExperiment(a);
    analysisRepository.deleteCompositeEntities(a.getAnalysisId());
    analysisInfoService.delete(a.getAnalysisId());
    a.getSample().stream()
        .map(CompositeEntity::getDonor)
        .map(Donor::getDonorId)
        .forEach(x -> donorService.delete(a.getStudy(), x));
    a.getFile().stream()
        .map(File::getObjectId)
        .forEach(x -> fileService.delete(x));
    analysisRepository.deleteAnalysis(a.getAnalysisId());
  }

  private void deleteExperiment(Analysis a){
    if (SequencingReadAnalysis.class.isInstance(a)){
      analysisRepository.deleteSequencingRead(a.getAnalysisId());
      sequencingReadInfoService.delete(a.getAnalysisId());
    } else if(VariantCallAnalysis.class.isInstance(a)){
      analysisRepository.deleteVariantCall(a.getAnalysisId());
      variantCallInfoService.delete(a.getAnalysisId());
    } else {
      throw new IllegalStateException("Unknown analysis type");
    }
  }

  /**
   * Given that  0 < numStudies < numAnalysesPerStudy, this test ensures that if the export service is requested for ONE
   * analysisId from EACH study, the response will return a list of size {@code numStudies}, where each element (which is
   * of type ExportedPayload) has exactly ONE payload (since there is only one analysis per study). This is a 2 in 1 test:
   * - it verifies the conversion of an existing analysis to a payload is correct, by submitting the
   * converted payload and comparing it with the original analysis
   * - it verifies the aggregation functionality of the export service, when given analysisIds belonging to
   * different studies
   */
  private void runFullLoopTest(Class<? extends Analysis> analysisClass, int numStudies, int numAnalysesPerStudy){
    val includeOtherIds = false;
    val includeAnalysisId = true;
    // Check the right parameters for this test are set
    assertCorrectConfig(numStudies, numAnalysesPerStudy);

    // Generate studies and there associated analyses
    val data = generateData(analysisClass, numStudies, numAnalysesPerStudy, includeAnalysisId, true);

    // [REDUCTION_TAG] Reduce the data so that there is one analysis for each study
    val reducedData = Maps.<String, Analysis>newHashMap();
    data.entrySet().stream()
        .filter(e -> !reducedData.containsKey(e.getKey()))
        .forEach(e -> {
          String studyId = e.getKey();
          List<? extends Analysis> analyses = e.getValue();
          int numAnalyses = analyses.size();
          int randomAnalysisPos = randomGenerator.generateRandomIntRange(0, numAnalyses);
          Analysis randomAnalysis = analyses.get(randomAnalysisPos);
          reducedData.put(studyId, randomAnalysis);
        });
    assertThat(reducedData.keySet()).hasSize(numStudies);
    assertThat(reducedData.values()).hasSize(numStudies);

    // Create a list of analysisIds that covers all the previously generated studies
    val requestedAnalysisIds = reducedData.values().stream()
        .map(Analysis::getAnalysisId)
        .collect(toImmutableList());
    assertThat(requestedAnalysisIds).hasSize(numStudies);

    // Export the analysis for the requested analysisIds
    val exportedPayloads =  exportService.exportPayload(requestedAnalysisIds, includeAnalysisId, includeOtherIds);

    // There should be an ExportedPayload object for each study
    assertThat(exportedPayloads).hasSize(numStudies);

    for (val exportedPayload : exportedPayloads){
      // There should be only 1 analysis for each study. Refer to the comment with REDUCTION_TAG.
      val studyId = exportedPayload.getStudyId();
      assertThat(exportedPayload.getPayloads()).hasSize(1);
      val payload = exportedPayload.getPayloads().get(0);
      val expectedAnalysis = reducedData.get(studyId);

      // Delete the previously created analysis so it can be created using the uploadService (frontdoor creation)
      deleteAnalysis(expectedAnalysis);

      // Depending on includeAnalysisId's value, either remove or keep the analysisId. Always keep the
      // other ids, so that it can be compared to the re-create analysis after submission
      massageAnalysisInplace(expectedAnalysis, includeAnalysisId, true);

      // Submit payload. Should create the same "otherIds" as the expected
      val actualAnalysis = submitPayload(studyId, payload, analysisClass);

      // Assert output analysis is correct
      assertAnalysis(actualAnalysis, expectedAnalysis);
    }
  }

  private <T extends Analysis> T submitPayload(String studyId, JsonNode payloadJson, Class<T> analysisClass){
    // Upload and check if successful
    val payload = toJson(payloadJson);
    val uploadStatus = uploadService.upload(studyId, payload, false);
    val status1 = fromStatus(uploadStatus, STATUS);
    assertThat(status1).isEqualTo(OK);
    val uploadId = fromStatus(uploadStatus, UPLOAD_ID);

    // Check Status and check if validated
    val statusResponse = uploadService.read(uploadId);
    val uploadState = resolveState(statusResponse.getState());
    assertThat(uploadState).isEqualTo(UploadStates.VALIDATED);

    // Save and check if successful
    val analysisResponse = uploadService.save(studyId, uploadId, true);
    val status2 = fromStatus(analysisResponse, STATUS);
    assertThat(status2).isEqualTo(OK);
    val analysisId = fromStatus(analysisResponse, ANALYSIS_ID);

    return analysisClass.cast(analysisService.read(analysisId));
  }

  /**
   * Generate {@code numStudies} studies and for each study generate {@code numAnalysisPerStudy} analyses, and put
   * everything in a map, where the keys are studyIds and the values are all the analyses for that study
   */
  private Map<String, List<? extends Analysis> > generateData(Class<? extends Analysis> analysisClass,
      int numStudies, int numAnalysesPerStudy, boolean includeAnalysisId, boolean includeOtherIds){

    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    val map = Maps.<String, List<? extends Analysis>>newHashMap();
    for (int s=0; s<numStudies ;s++){
      val studyId = studyGenerator.createRandomStudy();
      val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
      val analyses = range(0, numAnalysesPerStudy).boxed()
          .map(x -> analysisGenerator.createDefaultRandomAnalysis(analysisClass))
          .peek(x -> massageAnalysisInplace(x, includeAnalysisId, includeOtherIds))
          .collect(toImmutableList());
      map.put(studyId, analyses);
    }
    return ImmutableMap.copyOf(map);
  }

  private static Class<? extends Analysis> resolveAnalysisClass(Analysis a){
    if (resolveAnalysisType(a.getAnalysisType()) == SEQUENCING_READ){
      return SequencingReadAnalysis.class;
    } else if (resolveAnalysisType(a.getAnalysisType()) == VARIANT_CALL){
      return  VariantCallAnalysis.class;
    } else {
      throw new IllegalStateException("Unknown analysis type");
    }
  }

  private static void assertAnalysis(Analysis actualAnalysis, Analysis expectedAnalysis){
    assertThat(actualAnalysis.getAnalysisType()).isEqualTo(expectedAnalysis.getAnalysisType());

    assertThat(actualAnalysis.getAnalysisState()).isEqualTo(expectedAnalysis.getAnalysisState());
    assertThat(actualAnalysis.getInfo()).isEqualTo(expectedAnalysis.getInfo());

    assertThat(actualAnalysis.getAnalysisId()).isEqualTo(expectedAnalysis.getAnalysisId());

    // All of the actuals will have objectIds, sampleIds, specimenIds, donorIds, and analysisIds, however
    assertExperiment(actualAnalysis, expectedAnalysis);
    assertThat(actualAnalysis.getFile()).containsAll(expectedAnalysis.getFile());
    assertThat(actualAnalysis.getSample()).containsAll(expectedAnalysis.getSample());
    assertThat(actualAnalysis.getStudy()).isEqualTo(expectedAnalysis.getStudy());

    val actualFileObjectIds = collectObjectIds(actualAnalysis.getFile());
    val expectedFileObjectIds = collectObjectIds(expectedAnalysis.getFile());
    assertSetsMatch(actualFileObjectIds, expectedFileObjectIds);

    assertSubmitterIds(actualAnalysis, expectedAnalysis);
  }

  private static void assertExperiment(Analysis actual, Analysis expected){
    if (SequencingReadAnalysis.class.isInstance(actual)){
      assertSequencingReadExperiment(actual, expected);
    } else if (VariantCallAnalysis.class.isInstance(actual)){
      assertVariantCallExperiment(actual, expected);
    } else {
      throw new IllegalStateException("Unknown analysis type");
    }
  }

  private static void assertSequencingReadExperiment(Analysis a, Analysis e){
    val actual = SequencingReadAnalysis.class.cast(a);
    val expected = SequencingReadAnalysis.class.cast(e);
    assertThat(actual.getExperiment()).isEqualToComparingFieldByField(expected.getExperiment());
  }

  private static void assertVariantCallExperiment(Analysis a, Analysis e){
    val actual = VariantCallAnalysis.class.cast(a);
    val expected = VariantCallAnalysis.class.cast(e);
    assertThat(actual.getExperiment()).isEqualToComparingFieldByField(expected.getExperiment());
  }

  private static void assertSubmitterIds(Analysis actualAnalysis, Analysis expectedAnalysis){
    val actualSampleIds = collectSampleSubmitterIds(actualAnalysis.getSample());
    val expectedSampleIds = collectSampleSubmitterIds(expectedAnalysis.getSample());
    assertSetsMatch(actualSampleIds, expectedSampleIds);

    val actualSpecimenIds = collectSpecimenSubmitterIds(actualAnalysis.getSample());
    val expectedSpecimenIds = collectSpecimenSubmitterIds(expectedAnalysis.getSample());
    assertSetsMatch(actualSpecimenIds, expectedSpecimenIds);

    val actualDonorIds = collectDonorSubmitterIds(actualAnalysis.getSample());
    val expectedDonorIds = collectDonorSubmitterIds(expectedAnalysis.getSample());
    assertSetsMatch(actualDonorIds, expectedDonorIds);
  }

  private static Set<String> collectObjectIds(List<File> files){
    return files.stream().map(File::getObjectId).collect(toSet());
  }

  private static Set<String> collectSampleSubmitterIds(List<? extends Sample> samples){
    return samples.stream().map(Sample::getSampleSubmitterId).collect(toSet());
  }

  private static Set<String> collectSpecimenSubmitterIds(List<CompositeEntity> compositeEntities){
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
  private static String fromStatus( ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/"+key).asText("");
    return uploadId;
  }

  private static void massageAnalysisInplace(Analysis a, boolean includeAnalysisId, boolean includeOtherIds){
    if (!includeAnalysisId){
      a.setAnalysisId(EMPTY_STRING);
    }

    if (!includeOtherIds){
      a.setStudy(EMPTY_STRING);
      a.getFile()
          .forEach(x -> {
            x.setAnalysisId(EMPTY_STRING);
            x.setObjectId(EMPTY_STRING);
            x.setStudyId(EMPTY_STRING);
          });

      a.getSample().forEach( x->{
            x.setSampleId(EMPTY_STRING);
            x.setSpecimenId(EMPTY_STRING);
            x.getDonor().setDonorId(EMPTY_STRING);
            x.getDonor().setStudyId(EMPTY_STRING);
            x.getSpecimen().setDonorId(EMPTY_STRING);
            x.getSpecimen().setSpecimenId(EMPTY_STRING);
          }
      );

      val analysisClass = resolveAnalysisClass(a);
      if (analysisClass == SequencingReadAnalysis.class){
        val sra = SequencingReadAnalysis.class.cast(a);
        sra.getExperiment().setAnalysisId(EMPTY_STRING);
      } else if (analysisClass == VariantCallAnalysis.class){
        val vca = VariantCallAnalysis.class.cast(a);
        vca.getExperiment().setAnalysisId(EMPTY_STRING);
      }
    }
  }

  private static void assertCorrectConfig(int numStudies, int numAnalysesPerStudy){
    assertThat(numStudies).isLessThan(numAnalysesPerStudy);
    assertThat(numStudies).isGreaterThan(0);
  }

  private static void assertMatchingData(Map<String, List<? extends Analysis>> actualData, Map<String, List<? extends Analysis>> expectedData){
    assertSetsMatch(expectedData.keySet(), actualData.keySet());
    for (val studyId : expectedData.keySet()){
      val expectedAnalyses = expectedData.get(studyId);
      val actualAnalysisMap = groupUnique(actualData.get(studyId), Analysis::getAnalysisId); //Assumed that all analyses in the list are unique
      for (val expectedAnalysis : expectedAnalyses){
        assertThat(actualAnalysisMap).containsKey(expectedAnalysis.getAnalysisId());
        val actualAnalysis = actualAnalysisMap.get(expectedAnalysis.getAnalysisId());
        assertAnalysis(actualAnalysis, expectedAnalysis);
      }
    }
  }

}
