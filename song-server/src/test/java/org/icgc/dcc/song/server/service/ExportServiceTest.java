package org.icgc.dcc.song.server.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.model.enums.UploadStates;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;
import static org.icgc.dcc.song.server.utils.AnalysisGenerator.createAnalysisGenerator;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;

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

  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private AnalysisInfoService analysisInfoService;
  @Autowired private SequencingReadInfoService sequencingReadInfoService;
  @Autowired private DonorService donorService;
  @Autowired private FileService fileService;

  private final RandomGenerator randomGenerator = createRandomGenerator(ExportServiceTest.class.getSimpleName());

  /**
   * Delete the analysis
   */
  private void deleteAnalysis(SequencingReadAnalysis a){
    analysisRepository.deleteSequencingRead(a.getAnalysisId());
    analysisRepository.deleteCompositeEntities(a.getAnalysisId());
    analysisInfoService.delete(a.getAnalysisId());
    sequencingReadInfoService.delete(a.getAnalysisId());
    a.getSample().stream()
        .map(CompositeEntity::getDonor)
        .map(Donor::getDonorId)
        .forEach(x -> donorService.delete(a.getStudy(), x));
    a.getFile().stream()
        .map(File::getObjectId)
        .forEach(x -> fileService.delete(x));
    analysisRepository.deleteAnalysis(a.getAnalysisId());
  }

  @Test
  @SneakyThrows
  public void testFullLoop(){
    // Generate an analysis and create it (backdoor creation)
    val analysisGenerator = createAnalysisGenerator(DEFAULT_STUDY_ID, analysisService, randomGenerator);
    val expectedSeqAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();

    // Export the analysis
    val json =  exportService.exportPayload(expectedSeqAnalysis.getAnalysisId(), true, false);

    // Delete the previously created analysis so it can be created using the uploadService (frontdoor creation)
    deleteAnalysis(expectedSeqAnalysis);

    // Upload and check if successful
    val payload = JsonUtils.toJson(json);
    val uploadStatus = uploadService.upload(DEFAULT_STUDY_ID, payload, false);
    val status1 = fromStatus(uploadStatus, "status");
    assertThat(status1).isEqualTo("ok");
    val uploadId = fromStatus(uploadStatus, "uploadId");

    // Check Status and check if validated
    val statusResponse = uploadService.read(uploadId);
    val uploadState = resolveState(statusResponse.getState());
    assertThat(uploadState).isEqualTo(UploadStates.VALIDATED);

    // Save and check if successful
    val analysisResponse = uploadService.save(DEFAULT_STUDY_ID, uploadId, true);
    val status2 = fromStatus(analysisResponse, "status");
    assertThat(status2).isEqualTo("ok");
    val analysisId = fromStatus(analysisResponse, ANALYSIS_ID);

    // Get analysis and compare against expected
    val actualAnalysis = SequencingReadAnalysis.class.cast(analysisService.read(analysisId));
    assertThat(actualAnalysis.getExperiment()).isEqualToComparingFieldByField(expectedSeqAnalysis.getExperiment());
    assertThat(actualAnalysis.getAnalysisType()).isEqualTo(expectedSeqAnalysis.getAnalysisType());
    assertThat(actualAnalysis.getAnalysisId()).isEqualTo(expectedSeqAnalysis.getAnalysisId());
    assertThat(actualAnalysis.getAnalysisState()).isEqualTo(expectedSeqAnalysis.getAnalysisState());
    assertThat(actualAnalysis.getInfo()).isEqualTo(expectedSeqAnalysis.getInfo());
    assertThat(actualAnalysis.getStudy()).isEqualTo(expectedSeqAnalysis.getStudy());
    assertThat(actualAnalysis.getFile()).containsAll(expectedSeqAnalysis.getFile());
    assertThat(actualAnalysis.getSample()).containsAll(expectedSeqAnalysis.getSample());
  }

  @SneakyThrows
  public String fromStatus( ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/"+key).asText("");
    return uploadId;
  }


}
