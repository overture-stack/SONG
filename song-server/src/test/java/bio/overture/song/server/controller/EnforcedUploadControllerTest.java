package bio.overture.song.server.controller;

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_INCORRECT_VERSION;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

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

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({"test"})
@SpringBootTest(properties = "schemas.enforceLatest=true")
public class EnforcedUploadControllerTest extends AbstractEnforcedTester {

  /** Dependencies */
  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;

  /** Implementations */
  @Override
  protected WebApplicationContext getWebApplicationContext() {
    return webApplicationContext;
  }

  @Override
  protected StudyService getStudyService() {
    return studyService;
  }

  @Test
  public void enforceLatestSubmit_NonLatest_AnalysisTypeIncorrectVersion() {
    // Create a valid payload containing the non-latest version
    val payload = buildTestEnforcePayload(false);

    // Assert that submitting the payload while enforceLatest=true results in an
    // ANALYSIS_TYPE_INCORRECT_VERSION
    getEndpointTester()
        .submitPostRequestAnd(getStudyId(), payload)
        .assertServerError(ANALYSIS_TYPE_INCORRECT_VERSION);
  }

  @Test
  @SneakyThrows
  public void enforceLatestPublish_NonLatest_AnalysisTypeIncorrectVersion() {
    val analysisId = submit(true).getAnalysisId();

    // Register a new version, making the previously saved analysis out-dated
    registerAgain();

    // Assert that when the out-dated analysis is published when enforceLatest = true,
    // that ANALYSIS_TYPE_INCORRECT_VERSION server error is thrown
    getEndpointTester()
        .publishAnalysisPutRequestAnd(getStudyId(), analysisId)
        .assertServerError(ANALYSIS_TYPE_INCORRECT_VERSION);
  }

  @Test
  @SneakyThrows
  public void enforceLatestUpdate_NonLatest_AnalysisTypeIncorrectVersion() {
    val analysisId = submit(true).getAnalysisId();

    // Register a new version, making the previously saved analysis out-dated
    registerAgain();

    val a = analysisService.unsecuredDeepRead(analysisId);
    // Create an updateRequest body
    val request = new UpdateAnalysisRequest();
    val nonLatestAnalysisTypeId =
        AnalysisTypeId.builder()
            .name(getLatestAnalysisType().getName())
            .version(getLatestAnalysisType().getVersion() - 1)
            .build();
    request.setAnalysisType(nonLatestAnalysisTypeId);
    request.addData(a.getAnalysisData().getData());

    // Assert that when an analysisUpdate using an out-dated analysisType is attempted,
    // an ANALYSIS_TYPE_INCORRECT_VERSION server error is thrown
    getEndpointTester()
        .updateAnalysisPutRequestAnd(getStudyId(), analysisId, objectToTree(request))
        .assertServerError(ANALYSIS_TYPE_INCORRECT_VERSION);
  }

  @Test
  public void enforceLatestSubmit_Latest_Success() {
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(true);

    // Assert that submitting the payload while enforceLatest=true is successful
    getEndpointTester().submitPostRequestAnd(getStudyId(), payload).assertOk();
  }

  @Test
  @SneakyThrows
  public void enforceLatestPublish_Latest_Success() {
    val analysisId = submit(true).getAnalysisId();

    // Assert the error ResourceAccessException was thrown, indicating that the check for the
    // analysisType version
    // was successfull
    boolean completedAnalysisTypeCheck = false;
    try {
      getEndpointTester().publishAnalysisPutRequestAnd(getStudyId(), analysisId).getResponse();
    } catch (Exception e) {
      assertEquals(e.getCause().getClass(), ResourceAccessException.class);
      completedAnalysisTypeCheck = true;
    }
    assertTrue(completedAnalysisTypeCheck);
  }

  @Test
  @SneakyThrows
  public void enforceLatestUpdate_Latest_Success() {
    val analysisId = submit(true).getAnalysisId();
    val a = analysisService.unsecuredDeepRead(analysisId);

    // Create an updateRequest body
    val request = new UpdateAnalysisRequest();
    val nonLatestAnalysisTypeId =
        AnalysisTypeId.builder()
            .name(getLatestAnalysisType().getName())
            .version(getLatestAnalysisType().getVersion())
            .build();
    request.setAnalysisType(nonLatestAnalysisTypeId);
    request.addData(a.getAnalysisData().getData());

    // Assert success that when an analysisUpdate using the latest analysisType is attempted
    getEndpointTester()
        .updateAnalysisPutRequestAnd(getStudyId(), analysisId, objectToTree(request))
        .assertOk();
  }

  @Test
  @SneakyThrows
  public void enforceLatestUpdate_MissingVersion_Success() {
    val analysisId = submit(true).getAnalysisId();
    val a = analysisService.unsecuredDeepRead(analysisId);

    // Create an updateRequest body with a missing version
    val request = new UpdateAnalysisRequest();
    val nonLatestAnalysisTypeId =
        AnalysisTypeId.builder().name(getLatestAnalysisType().getName()).build();
    request.setAnalysisType(nonLatestAnalysisTypeId);
    request.addData(a.getAnalysisData().getData());

    // Assert success that when an analysisUpdate using the latest analysisType is attempted
    getEndpointTester()
        .updateAnalysisPutRequestAnd(getStudyId(), analysisId, objectToTree(request))
        .assertOk();
  }
}
