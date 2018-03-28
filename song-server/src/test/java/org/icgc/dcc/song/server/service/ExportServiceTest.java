package org.icgc.dcc.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.core.utils.Reductions;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;
import static org.icgc.dcc.song.server.utils.AnalysisGenerator.createAnalysisGenerator;
import static org.icgc.dcc.song.server.utils.StudyGenerator.createStudyGenerator;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.assertSetsMatch;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class ExportServiceTest {
  private static final String ANALYSIS_ID = "analysisId";

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

  @Test
  public void testFullLoopWithoutAnalysisId(){
    runFullLoopTest(SequencingReadAnalysis.class, false, false);
    runFullLoopTest(VariantCallAnalysis.class, false, false);
  }

  @Test
  public void testFullLoopWithAnalysisId(){
    runFullLoopTest(SequencingReadAnalysis.class,true, false);
    runFullLoopTest(VariantCallAnalysis.class,true, false);
  }

  // Test with multiple analysisIds, with 10 and 00
  // Test with studyId with multiple analysisIds, with 10 and 00

  // Test output

  @Test
  @Ignore
  public void testStudyExport(){
    val numAnalysisPerStudy = 7;
    int numStudies = 1;
    val analysisClass = SequencingReadAnalysis.class;
    val data = generateData(analysisClass, numStudies, numAnalysisPerStudy);
    assertThat(data.keySet()).hasSize(numStudies);
    val studyId = data.keySet().stream().findFirst().get();

    // Case0 - includeAnalysisId==0,  includeOtherIds==0
    val actualExportedPayloadList00 = exportService.exportPayloadsForStudy(studyId, false, false);
    assertThat(actualExportedPayloadList00).hasSize(numStudies);
    val actualExportedPayload00 = actualExportedPayloadList00.get(0);

    val expectedAnalyses = data.get(studyId);
    val expectedAnalysisMap = Reductions.groupUnique(expectedAnalyses, SequencingReadAnalysis::getAnalysisId);

    for (val payload : actualExportedPayload00.getPayloads()){
      val actualAnalysis = fromJson(payload,analysisClass);
      val expectedAnalysis = expectedAnalysisMap.get(actualAnalysis.getAnalysisId());
//      assertAnalysis(actualAnalysis, expectedAnalysis);

//      val expectedAnalysis = expectedAnalyses.
//      assertAnalysis(actualAnalysis, expectedAnalyses, false);

    }

//    assertThat(actualExportedPayload00.getStudyId()).isEqualTo(studyId);
//
//    // Test for includeAnalysisId == 0
//    assertThat(
//        actualExportedPayload00.getPayloads()
//            .stream()
//            .filter(x -> x.has(ANALYSIS_ID))
//            .count()).isEqualTo(0);
//
//    // Test for includeOtherIds == 0
//    assertThat(
//        actualExportedPayload00.getPayloads()
//            .stream()
//            .count()).isEqualTo(0);

    // Case1 - includeAnalysisId==0,  includeOtherIds==1
    // Case2 - includeAnalysisId==1,  includeOtherIds==0
    // Case3 - includeAnalysisId==1,  includeOtherIds==1

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

  private void runFullLoopTest(Class<? extends Analysis> analysisClass, boolean includeAnalysisId, boolean includeOtherIds){
    val studyId = DEFAULT_STUDY_ID;
    // Generate an analysis and create it (backdoor creation)
    val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
    val expectedAnalysis = analysisGenerator.createDefaultRandomAnalysis(analysisClass);

    // Export the analysis
    val exportedPayloads =  exportService.exportPayload(newArrayList(expectedAnalysis.getAnalysisId()), includeAnalysisId, includeOtherIds);
    val json = exportedPayloads.get(0).getPayloads().get(0);

    // Delete the previously created analysis so it can be created using the uploadService (frontdoor creation)
    deleteAnalysis(expectedAnalysis);

    // Submit payload
    val actualAnalysis = submitPayload(studyId, json, analysisClass);

    // Assert output analysis is correct
    assertAnalysis(actualAnalysis, expectedAnalysis, includeAnalysisId, includeOtherIds) ;
  }

  private <T extends Analysis> T submitPayload(String studyId, JsonNode payloadJson, Class<T> analysisClass){
    // Upload and check if successful
    val payload = toJson(payloadJson);
    val uploadStatus = uploadService.upload(studyId, payload, false);
    val status1 = fromStatus(uploadStatus, "status");
    assertThat(status1).isEqualTo("ok");
    val uploadId = fromStatus(uploadStatus, "uploadId");

    // Check Status and check if validated
    val statusResponse = uploadService.read(uploadId);
    val uploadState = resolveState(statusResponse.getState());
    assertThat(uploadState).isEqualTo(UploadStates.VALIDATED);

    // Save and check if successful
    val analysisResponse = uploadService.save(studyId, uploadId, true);
    val status2 = fromStatus(analysisResponse, "status");
    assertThat(status2).isEqualTo("ok");
    val analysisId = fromStatus(analysisResponse, ANALYSIS_ID);

    return analysisClass.cast(analysisService.read(analysisId));
  }

  /**
   * Generate {@code numStudies} studies and for each study generate {@code numAnalysisPerStudy} analyses, and put
   * everything in a map, where the keys are studyIds and the values are all the analyses for that study
   */
  private <T extends Analysis> Map<String, List<T> > generateData(Class<T> analysisClass, int numStudies, int numAnalysesPerStudy){
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    val list = Lists.<T>newArrayList();

    for (int s=0; s<numStudies ;s++){
      val studyId = studyGenerator.createRandomStudy();
      val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
      range(0, numAnalysesPerStudy).boxed()
          .map(x -> analysisGenerator.createDefaultRandomAnalysis(analysisClass))
          .forEach(list::add);
    }
    return list.stream().collect(groupingBy(Analysis::getStudy));
  }

  private static <T extends Analysis> void assertAnalysis(T actualAnalysis,
      T expectedAnalysis, boolean includeAnalysisId, boolean includeOtherIds){
    assertThat(actualAnalysis.getAnalysisType()).isEqualTo(expectedAnalysis.getAnalysisType());

    assertThat(actualAnalysis.getAnalysisState()).isEqualTo(expectedAnalysis.getAnalysisState());
    assertThat(actualAnalysis.getInfo()).isEqualTo(expectedAnalysis.getInfo());
    assertThat(actualAnalysis.getStudy()).isEqualTo(expectedAnalysis.getStudy());

    if (includeAnalysisId){
      assertThat(actualAnalysis.getAnalysisId()).isEqualTo(expectedAnalysis.getAnalysisId());

      // Since the analysisId will be missing from the payload, when its submitted, a random
      // analysisId will be generating, which will be different than the previously generated one.
      // Since the objectId of a File is dependent on the analysisId (refer to the IdService to see why),
      // the actual and expected objectIds will always mismatch when analysisIds are not included in the payload.
      val actualFileObjectIds = collectObjectIds(actualAnalysis.getFile());
      val expectedFileObjectIds = collectObjectIds(expectedAnalysis.getFile());
      assertSetsMatch(actualFileObjectIds, expectedFileObjectIds);
    }

    if(includeOtherIds){
      // All of the actuals will have objectIds, sampleIds, specimenIds, donorIds, and analysisIds, however
      assertExperiment(actualAnalysis, expectedAnalysis);
      assertThat(actualAnalysis.getFile()).containsAll(expectedAnalysis.getFile());
      assertThat(actualAnalysis.getSample()).containsAll(expectedAnalysis.getSample());
    }

    assertSubmitterIds(actualAnalysis, expectedAnalysis);
  }

  private static <T extends Analysis> void assertExperiment(T actual, T expected){
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


}
