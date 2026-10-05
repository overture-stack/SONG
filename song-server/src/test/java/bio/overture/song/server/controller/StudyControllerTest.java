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

import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static com.google.common.base.Strings.emptyToNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.EndpointTester;
import java.util.HashSet;
import javax.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
public class StudyControllerTest {

  @Autowired private StudyService studyService;

  // This was done because the autowired mockMvc wasn't working properly, it was getting http 403
  // errors
  @Autowired private WebApplicationContext webApplicationContext;
  private MockMvc mockMvc;

  private EndpointTester endpointTester;

  @BeforeEach
  public void beforeTest() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    this.endpointTester = createEndpointTester(mockMvc, true);
  }

  @Test
  public void saveStudyShouldValidateStudyId() {
    endpointTester.testPostError("/studies/123/", "{\"studyId\": \"456\"}", STUDY_ID_MISMATCH);
  }

  @Test
  public void getStudy_existingStudy_returnsStudy() {
    val studyId =
        createStudyGenerator(studyService, createRandomGenerator(getClass().getSimpleName()))
            .createRandomStudy();
    val expected = studyService.read(studyId);
    val actual =
        endpointTester
            .initWebRequest()
            .endpoint("studies/%s", studyId)
            .getAnd()
            .extractOneEntity(Study.class);
    // Study is serialized with NON_EMPTY inclusion, so empty strings are omitted.
    assertEquals(expected.getStudyId(), actual.getStudyId());
    assertEquals(emptyToNull(expected.getName()), actual.getName());
    assertEquals(emptyToNull(expected.getOrganization()), actual.getOrganization());
    assertEquals(emptyToNull(expected.getDescription()), actual.getDescription());
  }

  @Test
  public void getStudy_unknownStudy_notFound() {
    endpointTester
        .initWebRequest()
        .endpoint("studies/%s", "NO-SUCH-STUDY")
        .getAnd()
        .assertServerError(STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  @SneakyThrows
  public void getAllStudies_containsEveryStudyId() {
    val studyId =
        createStudyGenerator(studyService, createRandomGenerator(getClass().getSimpleName()))
            .createRandomStudy();
    val body =
        endpointTester.initWebRequest().endpoint("studies/all").getAnd().assertOk().getResponse();
    val studyIds = new HashSet<String>();
    readTree(body.getBody()).forEach(node -> studyIds.add(node.textValue()));
    assertTrue(studyIds.contains(studyId));
    assertEquals(new HashSet<>(studyService.findAllStudies()), studyIds);
  }

  @Test
  public void saveStudy_matchingStudyId_createsStudy() {
    val study = Study.builder().studyId("CREATED-BY-TEST").name("created").build();
    endpointTester
        .initWebRequest()
        .endpoint("studies/%s/", study.getStudyId())
        .body(study)
        .postAnd()
        .assertOk();
    assertTrue(studyService.isStudyExist(study.getStudyId()));
  }
}
