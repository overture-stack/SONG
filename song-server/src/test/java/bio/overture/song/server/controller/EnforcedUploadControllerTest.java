package bio.overture.song.server.controller;

import bio.overture.song.server.service.StudyService;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import static bio.overture.song.server.model.enums.UploadStates.VALIDATED;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATION_ERROR;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({"test"})
@SpringBootTest(properties = "schemas.enforceLatest=true")
public class EnforcedUploadControllerTest extends AbstractEnforcedTester {

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;

  @Override protected WebApplicationContext getWebApplicationContext() {
    return webApplicationContext;
  }

  @Override protected StudyService getStudyService() {
    return studyService;
  }

  @Test
  public void enforceLatestUpload_NonLatestAndEnforced_ValidationError() {
    val payload = buildTestEnforcePayload(false);
    assertUploadState(getStudyId(), payload, VALIDATION_ERROR);
  }

  @Test
  public void enforceLatestUpload_MissingVersionAndEnforced_Validated() {
    val payload = buildTestEnforcePayload(null);
    assertUploadState(getStudyId(), payload, VALIDATED);
  }

  @Test
  public void enforceLatestUpload_LatestAndNotEnforced_Validated() {
    val payload = buildTestEnforcePayload(true);
    assertUploadState(getStudyId(), payload, VALIDATED);
  }
}
