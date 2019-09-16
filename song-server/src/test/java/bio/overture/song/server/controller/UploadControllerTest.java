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

import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISSING;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.MAIN;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.TEST;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATED;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATION_ERROR;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.VARIANT_CALL;
import static bio.overture.song.server.utils.generator.PayloadGenerator.updateStudyInPayload;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Joiners.PATH;
import static org.junit.Assert.assertEquals;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.TestFiles;
import bio.overture.song.server.utils.generator.StudyGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import javax.transaction.Transactional;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Joiners.PATH;
import static org.junit.Assert.assertEquals;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISSING;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.TEST;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATED;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATION_ERROR;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.VARIANT_CALL;
import static bio.overture.song.server.utils.generator.PayloadGenerator.updateStudyInPayload;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles({"test"})
public class UploadControllerTest {

  private static final ResourceFetcher DOCUMENTS_FETCHER =
      ResourceFetcher.builder().resourceType(TEST).dataDir(Paths.get("documents/")).build();

  private static final ResourceFetcher LEGACY_SCHEMA_FETCHER =
      ResourceFetcher.builder()
          .resourceType(MAIN)
          .dataDir(Paths.get("schemas/analysis/legacy/"))
          .build();

  private static final String UPLOAD_TEST_DIR = "documents";
  private static final List<String> PAYLOAD_PATHS =
      newArrayList("variantcall-valid.json", "sequencingread-valid.json");
  private static final String DEFAULT_STUDY_ID = "ABC123";

  // This was done because the autowired mockMvc wasn't working properly, it was getting http 403
  // errors
  @Autowired private WebApplicationContext webApplicationContext;
  private MockMvc mockMvc;

  @Autowired private StudyService studyService;

  /** State */
  private RandomGenerator randomGenerator;

  private StudyGenerator studyGenerator;
  private EndpointTester endpointTester;

  @Before
  public void beforeEachTest() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    this.randomGenerator = createRandomGenerator(getClass().getSimpleName());
    this.studyGenerator = createStudyGenerator(studyService, randomGenerator);
    studyService.checkStudyExist(DEFAULT_STUDY_ID);
    this.endpointTester = createEndpointTester(mockMvc, true);
  }

  @Test
  public void mismatchedStudyInPayloadTest() {
    val nonExistingStudy = studyGenerator.generateNonExistingStudyId();
    streamPayloadNodes()
        .peek(x -> updateStudyInPayload(x, nonExistingStudy))
        .forEach(
            x ->
                runEndpointSongErrorTest(
                    format("/upload/%s/", DEFAULT_STUDY_ID), x, STUDY_ID_MISMATCH));
  }

  @Test
  public void missingStudyInPayloadTest() {
    streamPayloadNodes()
        .peek(x -> ((ObjectNode) x).remove(STUDY))
        .forEach(
            x ->
                runEndpointSongErrorTest(
                    format("/upload/%s/", DEFAULT_STUDY_ID), x, STUDY_ID_MISSING));
  }

  @Test
  @SneakyThrows
  @Transactional
  public void uploadPayload_missingAnalysisTypeName_schemaViolation() {
    // Prepare the payload
    val existingStudyId = studyGenerator.createRandomStudy();

    // Get the valid payload
    val payload = (ObjectNode) DOCUMENTS_FETCHER.readJsonNode("variantcall-valid.json");
    payload.put("study", existingStudyId);

    // Assert the payload is valid
    assertUploadState(existingStudyId, payload, VALIDATED);

    // Get the analysisTypeNode
    val analysisTypeNode = (ObjectNode) payload.path("analysisType");

    // Corrupt the payload by removing analysisType.name
    analysisTypeNode.remove("name");

    // Assert the payload with a missing analysisType.name field is invalid
    assertUploadState(existingStudyId, payload, VALIDATION_ERROR);

    // Add the name back to the payload, but remove the version (required)
    analysisTypeNode.put("name", VARIANT_CALL.getAnalysisTypeName());
    analysisTypeNode.remove("version");

    // Assert the payload with a missing analysisType.version field is invalid
    assertUploadState(existingStudyId, payload, VALIDATION_ERROR);

    // remove the analysisType node from the payload
    payload.remove("analysisType");

    // Assert the payload with a missing analysisType field is invalid
    assertUploadState(existingStudyId, payload, VALIDATION_ERROR);
  }

  @SneakyThrows
  private void assertUploadState(
      String studyId, JsonNode payload, UploadStates expectedUploadState) {
    // Upload the payload
    val response =
        endpointTester.syncUploadPostRequestAnd(studyId, payload).assertOk().getResponse();
    val uploadId = readTree(response.getBody()).path("uploadId").textValue();

    // assert the upload state
    val statusResponse =
        readTree(
            endpointTester
                .getUploadStatusGetRequestAnd(studyId, uploadId)
                .assertOk()
                .getResponse()
                .getBody());
    val actualUploadState = statusResponse.path("state").textValue();
    assertEquals(actualUploadState, expectedUploadState.getText());
  }

  @SneakyThrows
  private void runEndpointSongErrorTest(
      String endpointPath, JsonNode payload, ServerError expectedServerError) {
    val payloadString = toJson(payload);
    endpointTester.testPostError(endpointPath, payloadString, expectedServerError);
  }

  private static Stream<JsonNode> streamPayloadNodes() {
    return PAYLOAD_PATHS.stream()
        .map(x -> PATH.join(UPLOAD_TEST_DIR, x))
        .map(TestFiles::getJsonNodeFromClasspath);
  }
}
