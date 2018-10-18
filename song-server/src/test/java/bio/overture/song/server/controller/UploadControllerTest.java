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

package bio.overture.song.server.controller;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.StudyGenerator;
import bio.overture.song.server.utils.TestFiles;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Joiners.PATH;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISSING;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.PayloadGenerator.updateStudyInPayload;
import static bio.overture.song.server.utils.StudyGenerator.createStudyGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({ "dev", "test" })
public class UploadControllerTest {

  private static final String UPLOAD_TEST_DIR = "documents";
  private static final List<String> PAYLOAD_PATHS = newArrayList("variantcall-valid.json", "sequencingread-valid.json");
  private static final String DEFAULT_STUDY_ID = "ABC123";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private StudyService studyService;

  /**
   * State
   */
  private RandomGenerator randomGenerator;
  private StudyGenerator studyGenerator;
  private EndpointTester endpointTester;

  @Before
  public void beforeEachTest(){
    this.randomGenerator = createRandomGenerator(getClass().getSimpleName());
    this.studyGenerator = createStudyGenerator(studyService, randomGenerator);
    studyService.checkStudyExist(DEFAULT_STUDY_ID);
    this.endpointTester = createEndpointTester(mockMvc);
  }

  @Test
  public void mismatchedStudyInPayloadTest() {
    val nonExistingStudy = studyGenerator.generateNonExistingStudyId();
    streamPayloadNodes()
        .peek(x -> updateStudyInPayload(x, nonExistingStudy))
        .forEach(x -> runEndpointSongErrorTest(format("/upload/%s/", DEFAULT_STUDY_ID), x, STUDY_ID_MISMATCH));
  }

  @Test
  public void missingStudyInPayloadTest(){
    streamPayloadNodes().forEach(x ->
        runEndpointSongErrorTest(format("/upload/%s/", DEFAULT_STUDY_ID), x, STUDY_ID_MISSING) );
  }

  @SneakyThrows
  private void runEndpointSongErrorTest(String endpointPath, JsonNode payload, ServerError expectedServerError){
    val payloadString = toJson(payload);
    endpointTester.testPostError(endpointPath, payloadString, expectedServerError);
  }

  private static Stream<JsonNode> streamPayloadNodes(){
    return PAYLOAD_PATHS.stream()
        .map(x -> PATH.join(UPLOAD_TEST_DIR, x))
        .map(TestFiles::getJsonNodeFromClasspath);
  }

}
