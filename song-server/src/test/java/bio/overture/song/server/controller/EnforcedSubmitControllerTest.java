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

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_INCORRECT_VERSION;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.MAIN;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.TEST;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import bio.overture.song.core.model.AnalysisType;
import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.AnalysisTypeOptions;
import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.model.dto.UpdateAnalysisRequest;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.analysis.AnalysisService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Paths;
import java.util.List;
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
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@SpringBootTest(properties = "schemas.enforceLatest=true")
public class EnforcedSubmitControllerTest extends AbstractEnforcedTester {

  /** Dependencies */
  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;
  private static final ResourceFetcher DOCUMENTS_FETCHER =
      ResourceFetcher.builder().resourceType(TEST).dataDir(Paths.get("documents/")).build();
  private static final ResourceFetcher LEGACY_SCHEMA_FETCHER =
      ResourceFetcher.builder()
          .resourceType(MAIN)
          .dataDir(Paths.get("schemas/analysis/legacy/"))
          .build();
  private static final String VARIANT_CALL_LEGACY_NAME = "variantCall";
  private static final String VARIANT_CALL_LEGACY_SCHEMA_FILENAME = "variantCall.json";

  /** Implementations */
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
  public void testInvalidAnalysisType() {

    // Test invalid analysisType format
    val j1 =
        (ObjectNode)
            DOCUMENTS_FETCHER.readJsonNode("validation/variantcall-malformed-analysisType1.json");
    getEndpointTester()
        .submitPostRequestAnd(getStudyId(), j1)
        .assertServerError(MALFORMED_PARAMETER);

    // Test invalid analysisType format
    val j2 =
        (ObjectNode)
            DOCUMENTS_FETCHER.readJsonNode("validation/variantcall-malformed-analysisType2.json");
    getEndpointTester()
        .submitPostRequestAnd(getStudyId(), j2)
        .assertServerError(MALFORMED_PARAMETER);
  }

  @Test
  public void testInvalidFile() {
    // The pre-seeded legacy "variantCall" analysisType (the one this fixture's hardcoded
    // "analysisType" field targets) has no fileTypes restriction, so the fileType enum check
    // (ValidationService.validate lines 111-114) never runs. Register a new version that
    // restricts fileTypes to what this fixture actually uses, so that check is exercised too.
    // Since schemas.enforceLatest=true here, every submission below must target this new
    // version once it's registered.
    val legacyVariantCallSchema =
        LEGACY_SCHEMA_FETCHER.readJsonNode(VARIANT_CALL_LEGACY_SCHEMA_FILENAME);
    val restrictedVersion =
        getEndpointTester()
            .registerAnalysisTypePostRequestAnd(
                RegisterAnalysisTypeRequest.builder()
                    .name(VARIANT_CALL_LEGACY_NAME)
                    .schema(legacyVariantCallSchema)
                    .options(AnalysisTypeOptions.builder().fileTypes(List.of("VCF", "IDX")).build())
                    .build())
            .extractOneEntity(AnalysisType.class)
            .getVersion();

    val j = (ObjectNode) DOCUMENTS_FETCHER.readJsonNode("variantcall-valid.json");
    j.put("studyId", getStudyId());
    randomizeFileChecksums(j);
    ((ObjectNode) j.path("analysisType")).put("version", restrictedVersion);
    val s = (ObjectNode) j.get("files").get(0);
    s.put("fileType", "invalid");
    getEndpointTester().submitPostRequestAnd(getStudyId(), j).assertServerError(SCHEMA_VIOLATION);

    val j2 = (ObjectNode) DOCUMENTS_FETCHER.readJsonNode("variantcall-valid.json");
    j2.put("studyId", getStudyId());
    randomizeFileChecksums(j2);
    ((ObjectNode) j2.path("analysisType")).put("version", restrictedVersion);
    val s2 = (ObjectNode) j2.get("files").get(0);
    s2.put("fileAccess", "invalid");
    getEndpointTester().submitPostRequestAnd(getStudyId(), j2).assertServerError(SCHEMA_VIOLATION);

    val j3 = (ObjectNode) DOCUMENTS_FETCHER.readJsonNode("variantcall-valid.json");
    j3.put("studyId", getStudyId());
    randomizeFileChecksums(j3);
    ((ObjectNode) j3.path("analysisType")).put("version", restrictedVersion);
    val s3 = (ObjectNode) j3.get("files").get(0);
    s3.put("fileMd5sum", "invalid");
    getEndpointTester().submitPostRequestAnd(getStudyId(), j3).assertServerError(SCHEMA_VIOLATION);
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
    request.addData(toUpdatableData(a.getAnalysisData().getData()));

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
    request.addData(toUpdatableData(a.getAnalysisData().getData()));

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
    request.addData(toUpdatableData(a.getAnalysisData().getData()));

    // Assert success that when an analysisUpdate using the latest analysisType is attempted
    getEndpointTester()
        .updateAnalysisPutRequestAnd(getStudyId(), analysisId, objectToTree(request))
        .assertOk();
  }
}
