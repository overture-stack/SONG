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

import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;

import bio.overture.song.server.utils.EndpointTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({"test"})
public class StudyControllerTest {

  // This was done because the autowired mockMvc wasn't working properly, it was getting http 403
  // errors
  @Autowired private WebApplicationContext webApplicationContext;
  private MockMvc mockMvc;

  private EndpointTester endpointTester;

  @Before
  public void beforeTest() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    this.endpointTester = createEndpointTester(mockMvc, true);
  }

  @Test
  public void saveStudyShouldValidateStudyId() {
    endpointTester.testPostError("/studies/123/", "{\"studyId\": \"456\"}", STUDY_ID_MISMATCH);
  }
}
