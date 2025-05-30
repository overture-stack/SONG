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

package bio.overture.song.server.controller;

import static bio.overture.song.core.exceptions.ServerErrors.*;
import static bio.overture.song.core.utils.JsonUtils.*;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.TestConstants.*;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.SEQUENCING_READ;
import static bio.overture.song.server.utils.generator.PayloadGenerator.createPayloadGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.generator.PayloadGenerator;
import bio.overture.song.server.utils.generator.StudyGenerator;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@SpringBootTest(properties = "schemas.enforceLatest=false")
public class CorruptionSubmitControllerTest extends AbstractEnforcedTester {

  /** Dependencies */
  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private StudyService studyService;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(CorruptionSubmitControllerTest.class.getSimpleName());

  /** State */
  private PayloadGenerator payloadGenerator;

  private StudyGenerator studyGenerator;

  @Override
  protected WebApplicationContext getWebApplicationContext() {
    return webApplicationContext;
  }

  @Override
  protected StudyService getStudyService() {
    return studyService;
  }

  @Override
  protected boolean isLoggingEnabled() {
    return false;
  }

  @Override
  public void beforeEachTest() {
    this.payloadGenerator = createPayloadGenerator(randomGenerator);
    this.studyGenerator = createStudyGenerator(getStudyService(), randomGenerator);
    super.beforeEachTest();
  }

  @Test
  public void testNonMutated() {
    val studyId = randomStudy();
    val payload = randomPayload();
    payload.setStudyId(studyId);
    getEndpointTester()
        .submitPostRequestAnd(studyId, objectToTree(payload))
        .assertOk()
        .assertHasBody();

    modifyPayload(payload);

    getEndpointTester().submitPostRequestAnd(studyId, objectToTree(payload)).assertOk();
  }

  /**
   * Generates a payload with a unique new study, submits it, then modifies the original payload's
   * data to be inconsistent with the previous, and submits it again. This simulates the case a
   * second request is made where immutable data is mutated.
   */
  private void runTest(ServerError expectedError) {
    val studyGenerator = createStudyGenerator(getStudyService(), randomGenerator);
    val studyId = studyGenerator.createRandomStudy();
    val payload = randomPayload();
    payload.setStudyId(studyId);
    getEndpointTester()
        .submitPostRequestAnd(studyId, objectToTree(payload))
        .assertOk()
        .assertHasBody();

    val payload2 = modifyPayload(payload);

    getEndpointTester()
        .submitPostRequestAnd(studyId, objectToTree(payload2))
        .assertServerError(expectedError);
  }

  private String randomStudy() {
    return studyGenerator.createRandomStudy();
  }

  private Payload randomPayload() {
    return payloadGenerator.generateDefaultRandomPayload(SEQUENCING_READ);
  }

  private Payload modifyPayload(Payload payload) {
    val payload2 = fromJson(toJson(payload), Payload.class);
    return payload2;
  }
}
