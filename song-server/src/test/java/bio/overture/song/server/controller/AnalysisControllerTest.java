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

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static java.lang.String.format;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.core.model.AnalysisStateChange;
import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.model.StorageObject;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.service.StorageService;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.service.analysis.AnalysisServiceImpl;
import bio.overture.song.server.service.analysis.AnalysisServiceSender;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.generator.AnalysisGenerator;
import bio.overture.song.server.utils.generator.StudyGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
@Transactional
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(secure = false)
@SpringBootTest(properties = "schemas.enforceLatest=false")
public class AnalysisControllerTest {

  private static final Class<bio.overture.song.core.model.Analysis> ANALYSIS_DTO_CLASS =
      bio.overture.song.core.model.Analysis.class;

  private static final ResourceFetcher RESOURCE_FETCHER =
      ResourceFetcher.builder()
          .resourceType(ResourceFetcher.ResourceType.TEST)
          .dataDir(Paths.get("documents/updateAnalysis"))
          .build();

  // This was done because the autowired mockMvc wasn't working properly, it was getting http 403
  // errors
  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;

  @Autowired private AnalysisService analysisService;
  @Autowired private AnalysisServiceImpl internalAnalysisService;

  /** State */
  private RandomGenerator randomGenerator;

  private MockMvc mockMvc;
  private StudyGenerator studyGenerator;
  private EndpointTester endpointTester;
  private String studyId;
  private AnalysisGenerator analysisGenerator;
  private Analysis variantAnalysis;

  // Storage for mocked variables
  private StorageService actualStorageService;
  private AnalysisServiceImpl actualAnalysisService;

  @Before
  public void beforeEachTest() {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();
    this.endpointTester = createEndpointTester(mockMvc, true);
    this.randomGenerator = createRandomGenerator(getClass().getSimpleName());
    this.studyGenerator = createStudyGenerator(studyService, randomGenerator);
    this.studyId = studyGenerator.createRandomStudy();
    studyService.checkStudyExist(studyId);
    this.analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);

    // create a variantcall analysis
    this.variantAnalysis =
        analysisGenerator.createRandomAnalysis(
            () ->
                fromJson(
                    RESOURCE_FETCHER.readJsonNode("variantcall1-valid-payload.json"),
                    Payload.class));

    // register a new version of variant call
    endpointTester
        .registerAnalysisTypePostRequestAnd(
            RegisterAnalysisTypeRequest.builder()
                .schema(RESOURCE_FETCHER.readJsonNode("variantcall2-schema.json"))
                .name("variantCall")
                .build())
        .assertOk();

    endpointTester
        .registerAnalysisTypePostRequestAnd(
            RegisterAnalysisTypeRequest.builder()
                .schema(RESOURCE_FETCHER.readJsonNode("variantcall3-schema.json"))
                .name("variantCall")
                .build())
        .assertOk();

    val mockStorageService = mock(StorageService.class);
    val files = this.variantAnalysis.getFiles();
    for (val file : files) {
      val storageObject =
          StorageObject.builder()
              .fileMd5sum(file.getFileMd5sum())
              .fileSize(file.getFileSize())
              .objectId(file.getObjectId())
              .build();
      Mockito.when(mockStorageService.downloadObject(file.getObjectId())).thenReturn(storageObject);
      Mockito.when(mockStorageService.isObjectExist(file.getObjectId())).thenReturn(true);
    }

    actualAnalysisService =
        (AnalysisServiceImpl)
            ReflectionTestUtils.getField(analysisService, "internalAnalysisService");
    actualStorageService =
        (StorageService) ReflectionTestUtils.getField(internalAnalysisService, "storageService");

    ReflectionTestUtils.setField(internalAnalysisService, "storageService", mockStorageService);
    ReflectionTestUtils.setField(
        analysisService, "internalAnalysisService", internalAnalysisService);
  }

  @After
  public void removeMocks() {
    /* Replacing the mocked variables with the original objects is required to guarantee expected behaviour
     * in other test classes. Instances exist where the mocked variables are still in place for other tests
     * in other classes if you don't remove them.
     */
    ReflectionTestUtils.setField(internalAnalysisService, "storageService", actualStorageService);
    ReflectionTestUtils.setField(analysisService, "internalAnalysisService", actualAnalysisService);
  }

  @Test
  public void testCorrectImplementation() {
    val expectedClass = AnalysisServiceSender.class;
    assertTrue(
        format(
            "Expected class is %s, but actual was %s",
            expectedClass.getSimpleName(), analysisService.getClass().getSimpleName()),
        expectedClass.isInstance(analysisService));
  }

  @Test
  public void updateAnalysis_schemaNotUpdatedAndDataNotUpdated_SuccessNoUpdate() {
    // Execute an update request with the exact same data
    updateAnalysisWithFixture(
        studyId, variantAnalysis.getAnalysisId(), "variantcall1-no-update-request.json");

    // Check there were no updates
    assertAnalysisIdHasSameData(
        variantAnalysis.getAnalysisId(), "variantcall1-no-update-analysis-data.json");
  }

  @Test
  public void updateAnalysis_schemaNotUpdatedAndDataUpdatedAndInvalid_SchemaViolation() {
    // Assert that when the schema version is not changes, but the data is invalid, a
    // SCHEMA_VIOLATION exception is thrown
    assertUpdateAnalysisError(
        studyId,
        variantAnalysis.getAnalysisId(),
        "variantcall1-invalid-update-request.json",
        SCHEMA_VIOLATION);
  }

  @Test
  public void updateAnalysis_schemaNotUpdatedAndDataUpdatedAndValid_Success() {
    // Do not update the schema version but update the data
    updateAnalysisWithFixture(
        studyId, variantAnalysis.getAnalysisId(), "variantcall1-valid-update-request.json");

    // Assert the updated data matches what was expected
    assertAnalysisIdHasSameData(
        variantAnalysis.getAnalysisId(), "variantcall1-valid-update-analysis-data.json");
  }

  @Test
  public void updateAnalysis_schemaUpdatedAndDataNotUpdatedAndInvalid_SchemaViolation() {
    // Assert that when a request is made with an new schema version, but the data is not updated,
    // the requests errors with a SCHEMA_VIOLATION error
    assertUpdateAnalysisError(
        studyId,
        variantAnalysis.getAnalysisId(),
        "variantcall2-invalid-update-request.json",
        SCHEMA_VIOLATION);
  }

  @Test
  public void updateAnalysis_schemaUpdatedAndDataNotUpdatedAndValid_Success() {
    // update the data for variantCall:1
    updateAnalysisWithFixture(
        studyId, variantAnalysis.getAnalysisId(), "variantcall1-valid-update-request.json");

    // update the data for variantCall:2
    updateAnalysisWithFixture(
        studyId, variantAnalysis.getAnalysisId(), "variantcall2-valid-update-request.json");

    // Assert the 3rd update is not an update of data, but just the analysisType version
    assertJsonEquals(
        RESOURCE_FETCHER.readJsonNode("variantcall2-valid-update-request.json"),
        RESOURCE_FETCHER.readJsonNode("variantcall3-valid-update-request.json"),
        when(IGNORING_ARRAY_ORDER).whenIgnoringPaths("analysisType"));

    // update the data for variantCall:3 (this update is almost the same as the previous)
    updateAnalysisWithFixture(
        studyId, variantAnalysis.getAnalysisId(), "variantcall3-valid-update-request.json");

    // assert the update is valid for variantCall:3
    assertAnalysisIdHasSameData(
        variantAnalysis.getAnalysisId(), "variantcall2-valid-update-analysis-data.json");
  }

  @Test
  public void updateAnalysis_schemaUpdatedAndDataUpdatedAndInvalid_SchemaViolation() {
    // Assert that when the schema version is updated and data is updated incorrectly, a
    // SCHEMA_VIOLATION error occurs
    assertUpdateAnalysisError(
        studyId,
        variantAnalysis.getAnalysisId(),
        "variantcall2-invalid-update-request.json",
        SCHEMA_VIOLATION);
  }

  @Test
  public void updateAnalysis_schemaUpdatedAndDataUpdatedAndValid_Success() {
    // Update the schema version and update the data correctly
    updateAnalysisWithFixture(
        studyId, variantAnalysis.getAnalysisId(), "variantcall2-valid-update-request.json");

    // Assert the updated analysisData matches what is expected
    assertAnalysisIdHasSameData(
        variantAnalysis.getAnalysisId(), "variantcall2-valid-update-analysis-data.json");
  }

  @Test
  public void updateAnalysis_missingAnalysisTypeVersion_MalformedRequest() {
    // Assert that when an updateAnalysis request is made with a missing analysisType.version field,
    // a
    // MALFORMED_PARAMETER error occurs
    assertUpdateAnalysisError(
        studyId,
        variantAnalysis.getAnalysisId(),
        "variantcall1-invalid-update-request-missing-analysisTypeVersion.json",
        SCHEMA_VIOLATION);

    updateAnalysisWithFixture(
        studyId,
        variantAnalysis.getAnalysisId(),
        "variantcall1-valid-update-request-missing-analysisTypeVersion.json");
  }

  @Test
  public void updateAnalysis_missingAnalysisTypeName_MalformedRequest() {
    // Assert that when an updateAnalysis request is made with a missing analysisType.name field, a
    // MALFORMED_PARAMETER error occurs
    assertUpdateAnalysisError(
        studyId,
        variantAnalysis.getAnalysisId(),
        "variantcall1-invalid-update-request-missing-analysisTypeName.json",
        MALFORMED_PARAMETER);
  }

  @Test
  public void updateAnalysis_missingAnalysisType_MalformedRequest() {
    // Assert that when an updateAnalysis request is made with a missing analysisType field, a
    // MALFORMED_PARAMETER error occurs
    assertUpdateAnalysisError(
        studyId,
        variantAnalysis.getAnalysisId(),
        "variantcall2-valid-update-analysis-data.json",
        MALFORMED_PARAMETER);
  }

  @Test
  public void updateAnalysis_nonExistingAnalysisType_AnalysisTypeNotFound() {
    // Assert that when an updateAnalysis request is made with a non-existing analysisType NAME, an
    // ANALYSIS_TYPE_NOT_FOUND error is thrown
    assertUpdateAnalysisError(
        studyId,
        variantAnalysis.getAnalysisId(),
        "variantcall1-invalid-update-request-analysisTypeName.json",
        ANALYSIS_TYPE_NOT_FOUND);

    // Assert that when an updateAnalysis request is made with a non-existing analysisType VERSION,
    // an ANALYSIS_TYPE_NOT_FOUND error is thrown
    assertUpdateAnalysisError(
        studyId,
        variantAnalysis.getAnalysisId(),
        "variantcall1-invalid-update-request-analysisTypeVersion.json",
        ANALYSIS_TYPE_NOT_FOUND);
  }

  @Test
  public void updateAnalysis_nonExistingStudy_StudyNotFound() {
    val nonExistingStudyId = studyGenerator.generateNonExistingStudyId();
    assertUpdateAnalysisError(
        nonExistingStudyId,
        variantAnalysis.getAnalysisId(),
        "variantcall1-valid-update-request.json",
        STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void updateAnalysis_nonExistingAnalysis_AnalysisNotFound() {
    val nonExistingAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    assertUpdateAnalysisError(
        studyId,
        nonExistingAnalysisId,
        "variantcall1-valid-update-request.json",
        ANALYSIS_ID_NOT_FOUND);
  }

  @Test
  public void updateAnalysis_studyIdAndAnalysisIdNotRelated_EntityNotRelatedToStudy() {
    val differentExistingStudyId = studyGenerator.createRandomStudy();
    assertNotEquals(differentExistingStudyId, studyId);
    assertTrue(studyService.isStudyExist(differentExistingStudyId));

    assertUpdateAnalysisError(
        differentExistingStudyId,
        variantAnalysis.getAnalysisId(),
        "variantcall1-valid-update-request.json",
        ENTITY_NOT_RELATED_TO_STUDY);
  }

  @Test
  public void updateAnalysis_illegalData_SchemaViolation() {
    // Assert that when an update request is made containing study, file, sample, specimen, etc, it
    // gets invalidated against the rendered updateAnalysis schema
    Stream.of(
            "variantcall1-valid-payload.json",
            "variantcall1-invalid-update-request-hasAnalysisId.json",
            "variantcall1-invalid-update-request-hasAnalysisState.json",
            "variantcall1-invalid-update-request-hasAnalysisTypeId.json",
            "variantcall1-invalid-update-request-hasFileField.json",
            "variantcall1-invalid-update-request-hasSampleField.json",
            "variantcall1-invalid-update-request-hasStudyField.json")
        .forEach(
            f -> {
              log.info("Testing '{}'", f);
              assertUpdateAnalysisError(
                  studyId, variantAnalysis.getAnalysisId(), f, SCHEMA_VIOLATION);
            });
  }

  @Test
  public void getAnalysis_containsDateInfo() {
    val actualAnalysis =
        endpointTester
            .getAnalysisByIdAnd(
                this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId())
            .extractOneEntity(ANALYSIS_DTO_CLASS);
    assertEquals(this.variantAnalysis.getCreatedAt(), actualAnalysis.getCreatedAt());
    assertEquals(this.variantAnalysis.getUpdatedAt(), actualAnalysis.getUpdatedAt());
    assertNull(actualAnalysis.getPublishedAt());
    assertNull(actualAnalysis.getFirstPublishedAt());
  }

  @Test
  public void updateAnalysis_updatesDateInfo() {
    val initialUpdatedAt = this.variantAnalysis.getUpdatedAt();

    updateAnalysisWithFixture(
        studyId, variantAnalysis.getAnalysisId(), "variantcall1-valid-update-request.json");

    val actualAnalysis =
        endpointTester
            .getAnalysisByIdAnd(
                this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId())
            .extractOneEntity(ANALYSIS_DTO_CLASS);

    assertEquals(this.variantAnalysis.getCreatedAt(), actualAnalysis.getCreatedAt());
    assertTrue(initialUpdatedAt.isBefore(actualAnalysis.getUpdatedAt()));
  }

  @Test
  public void suppressAnalysis_updatesDateInfo() {
    val initialUpdatedAt = this.variantAnalysis.getUpdatedAt();
    endpointTester
        .suppressAnalysisByIdAnd(
            this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId())
        .assertOk();
    val actualAnalysis =
        endpointTester
            .getAnalysisByIdAnd(
                this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId())
            .extractOneEntity(ANALYSIS_DTO_CLASS);
    assertEquals(this.variantAnalysis.getCreatedAt(), actualAnalysis.getCreatedAt());
    assertTrue(initialUpdatedAt.isBefore(actualAnalysis.getUpdatedAt()));
    assertEquals(AnalysisStates.SUPPRESSED, actualAnalysis.getAnalysisState());
  }

  @Test
  public void publishAnalysis_updatesDateInfo() {
    val initialUpdatedAt = this.variantAnalysis.getUpdatedAt();

    endpointTester
        .publishAnalysisByIdAnd(
            this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId(), false)
        .assertOk();

    val actualAnalysis =
        endpointTester
            .getAnalysisByIdAnd(
                this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId())
            .extractOneEntity(ANALYSIS_DTO_CLASS);
    assertEquals(this.variantAnalysis.getCreatedAt(), actualAnalysis.getCreatedAt());
    assertTrue(initialUpdatedAt.isBefore(actualAnalysis.getUpdatedAt()));
    assertEquals(AnalysisStates.PUBLISHED, actualAnalysis.getAnalysisState());
    assertNotNull(actualAnalysis.getFirstPublishedAt());
    assertNotNull(actualAnalysis.getPublishedAt());
    assertEquals(actualAnalysis.getFirstPublishedAt(), actualAnalysis.getPublishedAt());
  }

  @Test
  public void getAnalysisByStudy_hasDateInfo() {
    val initialUpdatedAt = this.variantAnalysis.getUpdatedAt();

    endpointTester
        .publishAnalysisByIdAnd(
            this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId(), false)
        .assertOk();
    val response =
        endpointTester
            .getAnalysesForStudyAnd(this.variantAnalysis.getStudyId(), "PUBLISHED")
            .extractManyEntities(ANALYSIS_DTO_CLASS);

    assertEquals(response.size(), 1);
    response.forEach(
        analysis -> {
          assertEquals(this.variantAnalysis.getCreatedAt(), analysis.getCreatedAt());
          assertTrue(initialUpdatedAt.isBefore(analysis.getUpdatedAt()));
          assertEquals(AnalysisStates.PUBLISHED, analysis.getAnalysisState());
          assertNotNull(analysis.getFirstPublishedAt());
          assertNotNull(analysis.getPublishedAt());
          assertEquals(analysis.getFirstPublishedAt(), analysis.getPublishedAt());
        });
  }

  @Test
  public void repeatedStateChanges_hasSortedStateHistory() {
    val initialUpdatedAt = this.variantAnalysis.getUpdatedAt();

    endpointTester
        .publishAnalysisByIdAnd(
            this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId(), false)
        .assertOk();
    endpointTester
        .unpublishAnalysisByIdAnd(
            this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId())
        .assertOk();
    endpointTester
        .publishAnalysisByIdAnd(
            this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId(), false)
        .assertOk();
    endpointTester
        .suppressAnalysisByIdAnd(
            this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId())
        .assertOk();

    val actualAnalysis =
        endpointTester
            .getAnalysisByIdAnd(
                this.variantAnalysis.getStudyId(), this.variantAnalysis.getAnalysisId())
            .extractOneEntity(ANALYSIS_DTO_CLASS);
    assertEquals(this.variantAnalysis.getCreatedAt(), actualAnalysis.getCreatedAt());
    assertTrue(initialUpdatedAt.isBefore(actualAnalysis.getUpdatedAt()));
    assertEquals(AnalysisStates.SUPPRESSED, actualAnalysis.getAnalysisState());
    assertNotNull(actualAnalysis.getFirstPublishedAt());
    assertNotNull(actualAnalysis.getPublishedAt());
    assertTrue(actualAnalysis.getFirstPublishedAt().isBefore(actualAnalysis.getPublishedAt()));

    // State History Assertions
    assertEquals(actualAnalysis.getAnalysisStateHistory().size(), 4);
    val stateHistory = new AnalysisStateChange[4];
    actualAnalysis.getAnalysisStateHistory().toArray(stateHistory);
    assertTrue(stateHistory[0].getUpdatedAt().isBefore(stateHistory[1].getUpdatedAt()));
    assertTrue(stateHistory[1].getUpdatedAt().isBefore(stateHistory[2].getUpdatedAt()));
    assertTrue(stateHistory[2].getUpdatedAt().isBefore(stateHistory[3].getUpdatedAt()));
  }

  private void assertAnalysisIdHasSameData(String analysisId, String analysisDataFixtureFilename) {
    val actualAnalysis = analysisService.unsecuredDeepRead(analysisId);
    val actualData = actualAnalysis.getAnalysisData().getData();
    val expectedData = RESOURCE_FETCHER.readJsonNode(analysisDataFixtureFilename);
    assertJsonEquals(expectedData, actualData, when(IGNORING_ARRAY_ORDER));
  }

  private JsonNode updateAnalysisWithFixture(
      String studyId, String analysisId, String updateRequestFixtureFilename) {
    val update_request = RESOURCE_FETCHER.readJsonNode(updateRequestFixtureFilename);
    endpointTester.updateAnalysisPutRequestAnd(studyId, analysisId, update_request).assertOk();
    return update_request;
  }

  private void assertUpdateAnalysisError(
      String studyId,
      String analysisId,
      String updateRequestFixtureFilename,
      ServerError serverError) {
    val update_request = RESOURCE_FETCHER.readJsonNode(updateRequestFixtureFilename);
    endpointTester
        .updateAnalysisPutRequestAnd(studyId, analysisId, update_request)
        .assertServerError(serverError);
  }
}
