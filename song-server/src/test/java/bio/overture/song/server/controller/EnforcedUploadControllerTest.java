package bio.overture.song.server.controller;

import bio.overture.song.core.exceptions.SongError;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.analysis.AnalysisTypeId;
import bio.overture.song.server.model.dto.UpdateAnalysisRequest;
import bio.overture.song.server.service.AnalysisService;
import bio.overture.song.server.service.StudyService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertNotEquals;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_INCORRECT_VERSION;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_ID;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATED;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATION_ERROR;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({"test"})
@SpringBootTest(properties = "schemas.enforceLatest=true")
public class EnforcedUploadControllerTest extends AbstractEnforcedTester {

  /**
   * Dependencies
   */
  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;

  /**
   * Implementations
   */
  @Override protected WebApplicationContext getWebApplicationContext() {
    return webApplicationContext;
  }

  @Override protected StudyService getStudyService() {
    return studyService;
  }

  @Test
  public void enforceLatestUpload_NonLatestAndEnforced_ValidationError() {
    // Create a valid payload containing and outdated version
    val payload = buildTestEnforcePayload(false);

    // Upload the payload and assert a validation error results when enforceLatest=true
    assertUploadState(getStudyId(), payload, VALIDATION_ERROR);
  }

  @Test
  public void enforceLatestUpload_MissingVersionAndEnforced_Validated() {
    // Create a valid payload missing the version field
    val payload = buildTestEnforcePayload(null);

    // Upload the payload and assert successful validation when enforceLatest=true
    assertUploadState(getStudyId(), payload, VALIDATED);
  }

  @Test
  public void enforceLatestUpload_LatestAndEnforced_Validated() {
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert successful validation when enforceLatest=true
    assertUploadState(getStudyId(), payload, VALIDATED);
  }

  @Test
  public void enforceLatestSave_NonLatestAndEnforced_AnalysisTypeIncorrectVersion(){
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert successful validation when enforceLatest=true
    val uploadId = assertUploadState(getStudyId(), payload, VALIDATED);

    // Register a new version, making the previously upload outdated
    registerAgain();

    // Assert that saving the upload while enforceLatest=true results in an ANALYSIS_TYPE_INCORRECT_VERSION
    getEndpointTester().saveUploadPostRequestAnd(getStudyId(), uploadId )
        .assertServerError(ANALYSIS_TYPE_INCORRECT_VERSION);
  }

  @Test
  @SneakyThrows
  public void enforceLatestPublish_NonLatestAndEnforced_AnalysisTypeIncorrectVersion(){
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert its validated
    val uploadId = assertUploadState(getStudyId(), payload, VALIDATED);

    // Save the upload
    val saveStatus = JsonUtils.readTree(getEndpointTester().saveUploadPostRequestAnd(getStudyId(), uploadId )
        .assertOk()
        .getResponse()
        .getBody());
    val analysisId = saveStatus.path(ANALYSIS_ID).textValue();

    // Register a new version, making the previously saved analysis out-dated
    registerAgain();

    // Assert that when the out-dated analysis is published when enforceLatest = true,
    // that ANALYSIS_TYPE_INCORRECT_VERSION server error is thrown
    getEndpointTester().publishAnalysisPutRequestAnd(getStudyId(), analysisId)
        .assertServerError(ANALYSIS_TYPE_INCORRECT_VERSION);
  }

  @Test
  @SneakyThrows
  public void enforceLatestAnalysisUpdate_NonLatestAndEnforced_AnalysisTypeIncorrectVersion(){
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert its validated
    val uploadId = assertUploadState(getStudyId(), payload, VALIDATED);

    // Save the upload
    val saveStatus = JsonUtils.readTree(getEndpointTester().saveUploadPostRequestAnd(getStudyId(), uploadId )
        .assertOk()
        .getResponse()
        .getBody());
    val analysisId = saveStatus.path(ANALYSIS_ID).textValue();

    // Register a new version, making the previously saved analysis out-dated
    registerAgain();

    val a = analysisService.unsecuredDeepRead(analysisId);
    // Create an updateRequest body
    val request =new UpdateAnalysisRequest();
    val nonLatestAnalysisTypeId = AnalysisTypeId.builder()
        .name(getLatestAnalysisType().getName())
        .version(getLatestAnalysisType().getVersion()-1)
        .build();
    request.setAnalysisType(nonLatestAnalysisTypeId);
    request.addData(a.getAnalysisData().getData());

    // Assert that when an analysisUpdate using an out-dated analysisType is attempted,
    // an ANALYSIS_TYPE_INCORRECT_VERSION server error is thrown
    getEndpointTester().updateAnalysisPutRequestAnd(getStudyId(), analysisId, objectToTree(request))
        .assertServerError(ANALYSIS_TYPE_INCORRECT_VERSION);
  }

  @Test
  public void enforceLatestSave_LatestAndEnforced_Success(){
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert successful validation when enforceLatest=true
    val uploadId = assertUploadState(getStudyId(), payload, VALIDATED);

    // Assert that saving the upload while enforceLatest=true is successful
    getEndpointTester().saveUploadPostRequestAnd(getStudyId(), uploadId )
        .assertOk();
  }

  @Test
  @SneakyThrows
  public void enforceLatestPublish_LatestAndEnforced_Success(){
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert its validated
    val uploadId = assertUploadState(getStudyId(), payload, VALIDATED);

    // Save the upload
    val saveStatus = JsonUtils.readTree(getEndpointTester().saveUploadPostRequestAnd(getStudyId(), uploadId )
        .assertOk()
        .getResponse()
        .getBody());
    val analysisId = saveStatus.path(ANALYSIS_ID).textValue();

    // Assert that ANALYSIS_TYPE_INCORRECT_VERSION is NOT thrown when the latest analysis
    // is published when enforceLatest = true.
    // Since there were no files uploaded, an error will occur anyways, however the version check should happen first
    val songError = SongError.parseErrorResponse(getEndpointTester().publishAnalysisPutRequestAnd(getStudyId(), analysisId)
        .getResponse());
    assertNotEquals(songError.getErrorId(), ANALYSIS_TYPE_INCORRECT_VERSION);
  }

  @Test
  @SneakyThrows
  public void enforceLatestUpdate_LatestAndEnforced_Success(){
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert its validated
    val uploadId = assertUploadState(getStudyId(), payload, VALIDATED);

    // Save the upload
    val saveStatus = JsonUtils.readTree(getEndpointTester().saveUploadPostRequestAnd(getStudyId(), uploadId )
        .assertOk()
        .getResponse()
        .getBody());
    val analysisId = saveStatus.path(ANALYSIS_ID).textValue();
    val a = analysisService.unsecuredDeepRead(analysisId);

    // Create an updateRequest body
    val request =new UpdateAnalysisRequest();
    val nonLatestAnalysisTypeId = AnalysisTypeId.builder()
        .name(getLatestAnalysisType().getName())
        .version(getLatestAnalysisType().getVersion())
        .build();
    request.setAnalysisType(nonLatestAnalysisTypeId);
    request.addData(a.getAnalysisData().getData());

    // Assert success that when an analysisUpdate using the latest analysisType is attempted
    getEndpointTester().updateAnalysisPutRequestAnd(getStudyId(), analysisId, objectToTree(request))
        .assertOk();
  }

  @Test
  @SneakyThrows
  public void enforceLatestUpdate_MissingVersionAndEnforced_Success(){
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert its validated
    val uploadId = assertUploadState(getStudyId(), payload, VALIDATED);

    // Save the upload
    val saveStatus = JsonUtils.readTree(getEndpointTester().saveUploadPostRequestAnd(getStudyId(), uploadId )
        .assertOk()
        .getResponse()
        .getBody());
    val analysisId = saveStatus.path(ANALYSIS_ID).textValue();
    val a = analysisService.unsecuredDeepRead(analysisId);

    // Create an updateRequest body with a missing version
    val request =new UpdateAnalysisRequest();
    val nonLatestAnalysisTypeId = AnalysisTypeId.builder()
        .name(getLatestAnalysisType().getName())
        .build();
    request.setAnalysisType(nonLatestAnalysisTypeId);
    request.addData(a.getAnalysisData().getData());

    // Assert success that when an analysisUpdate using the latest analysisType is attempted
    getEndpointTester().updateAnalysisPutRequestAnd(getStudyId(), analysisId, objectToTree(request))
        .assertOk();
  }

}
