package bio.overture.song.server.controller;

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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.WebApplicationContext;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_ID;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATED;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({"test"})
@SpringBootTest(properties = "schemas.enforceLatest=false")
public class NonEnforcedUploadControllerTest extends AbstractEnforcedTester {

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;

  @Override protected WebApplicationContext getWebApplicationContext() {
    return webApplicationContext;
  }

  @Override protected StudyService getStudyService() {
    return studyService;
  }

  @Test
  public void nonEnforceLatestUpload_NonLatestAndNotEnforced_ValidationError() {
    val payload = buildTestEnforcePayload(false);
    assertUploadState(getStudyId(), payload, VALIDATED);
  }

  @Test
  public void nonEnforceLatestUpload_MissingVersionAndNotEnforced_Validated() {
    val payload = buildTestEnforcePayload(null);
    assertUploadState(getStudyId(), payload, VALIDATED);
  }

  @Test
  public void nonEnforceLatestUpload_LatestAndNotEnforced_Validated() {
    val payload = buildTestEnforcePayload(true);
    assertUploadState(getStudyId(), payload, VALIDATED);
  }

  @Test
  @SneakyThrows
  public void nonEnforcedLatestUpdate_NonLatest_Success(){
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

    // Assert that when an analysisUpdate using an out-dated analysisType is successful
    getEndpointTester().updateAnalysisPutRequestAnd(getStudyId(), analysisId, objectToTree(request))
        .assertOk();
  }

  @Test
  @SneakyThrows
  public void nonEnforcedLatestUpdate_Latest_Success(){
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
  public void nonEnforcedLatestUpdate_Missing_Success(){
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

  @Test
  @SneakyThrows
  public void nonEnforcedLatestPublish_Latest_Success(){
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

    // Assert the error ResourceAccessException was thrown, indicating that the check for the analysisType version
    // was successfull
    boolean completedAnalysisTypeCheck = false;
    try {
      getEndpointTester().publishAnalysisPutRequestAnd(getStudyId(), analysisId).getResponse();
    } catch (Exception e){
      assertEquals(e.getCause().getClass(), ResourceAccessException.class);
      completedAnalysisTypeCheck = true;
    }
    assertTrue(completedAnalysisTypeCheck);
  }

  @Test
  @SneakyThrows
  public void nonEnforcedLatestPublish_NonLatest_Success(){
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

    // Assert the error ResourceAccessException was thrown, indicating that the check for the analysisType version
    // was successfull
    boolean completedAnalysisTypeCheck = false;
    try {
      getEndpointTester().publishAnalysisPutRequestAnd(getStudyId(), analysisId).getResponse();
    } catch (Exception e){
      assertEquals(e.getCause().getClass(), ResourceAccessException.class);
      completedAnalysisTypeCheck = true;
    }
    assertTrue(completedAnalysisTypeCheck);
  }

  @Test
  public void nonEnforcedLatestSave_Latest_Success(){
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert successful validation when enforceLatest=true
    val uploadId = assertUploadState(getStudyId(), payload, VALIDATED);

    // Assert that saving the upload while enforceLatest=true is successful
    getEndpointTester().saveUploadPostRequestAnd(getStudyId(), uploadId )
        .assertOk();
  }

  @Test
  public void nonEnforcedLatestSave_NonLatest_Success(){
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Upload the payload and assert successful validation when enforceLatest=true
    val uploadId = assertUploadState(getStudyId(), payload, VALIDATED);

    // Register a new version, making the previously upload outdated
    registerAgain();

    // Assert that saving the upload while enforceLatest=false is successful
    getEndpointTester().saveUploadPostRequestAnd(getStudyId(), uploadId )
        .assertOk();

  }
}
