package bio.overture.song.server.controller;

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.utils.EndpointTester;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Paths;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests PATCH /studies/{studyId}/analysis/{analysisId}, which applies an RFC 7386 JSON merge patch
 * to the analysis data and validates the result against the analysis's current schema.
 *
 * <p>The merge-patch library used today (fge json-patch) is abandoned and will be replaced, so
 * these tests pin the merge semantics: objects merge recursively, null removes a field, arrays and
 * scalars are replaced whole.
 */
@Transactional
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AnalysisPatchControllerTest {

  private static final ResourceFetcher RESOURCE_FETCHER =
      ResourceFetcher.builder()
          .resourceType(ResourceFetcher.ResourceType.TEST)
          .dataDir(Paths.get("documents/contract"))
          .build();

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;
  @Autowired private EntityManager entityManager;

  private EndpointTester endpointTester;
  private String studyId;
  private String analysisId;

  @BeforeEach
  public void beforeEachTest() {
    this.endpointTester =
        createEndpointTester(webAppContextSetup(webApplicationContext).build(), true);
    val randomGenerator = createRandomGenerator(getClass().getSimpleName());
    this.studyId = createStudyGenerator(studyService, randomGenerator).createRandomStudy();
    val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
    this.analysisId =
        analysisGenerator
            .createRandomAnalysis(
                () ->
                    fromJson(
                        RESOURCE_FETCHER.readJsonNode("variantcall-payload.json"), Payload.class))
            .getAnalysisId();
  }

  /** Reads the stored analysis data after flushing, so the result reflects the database. */
  private JsonNode readStoredData() {
    entityManager.flush();
    entityManager.clear();
    return analysisService.unsecuredDeepRead(analysisId).getAnalysisData().getData();
  }

  @SneakyThrows
  private static JsonNode json(String text) {
    return readTree(text);
  }

  private void patchOk(String patchJson) {
    endpointTester.patchUpdateAnalysisRequestAnd(studyId, analysisId, json(patchJson)).assertOk();
  }

  @Test
  public void patch_scalarField_replacesOnlyThatField() {
    val before = readStoredData();
    patchOk("{\"experiment\": {\"variantCallingTool\": \"patched tool\"}}");

    val after = readStoredData();
    assertEquals("patched tool", after.at("/experiment/variantCallingTool").textValue());
    assertEquals(
        before.at("/experiment/matchedNormalSampleSubmitterId"),
        after.at("/experiment/matchedNormalSampleSubmitterId"));
    assertEquals(before.path("info"), after.path("info"));
  }

  @Test
  public void patch_nullValue_removesField() {
    patchOk("{\"experiment\": {\"extraField\": \"added\"}}");
    assertEquals("added", readStoredData().at("/experiment/extraField").textValue());

    patchOk("{\"experiment\": {\"extraField\": null}}");
    assertFalse(readStoredData().path("experiment").has("extraField"));
  }

  @Test
  public void patch_topLevelNull_removesTopLevelField() {
    assertTrue(readStoredData().has("info"));
    patchOk("{\"info\": null}");
    assertFalse(readStoredData().has("info"));
  }

  @Test
  public void patch_array_replacedWhole() {
    patchOk("{\"experiment\": {\"tags\": [\"a\", \"b\", \"c\"]}}");
    patchOk("{\"experiment\": {\"tags\": [\"z\"]}}");
    assertEquals(json("[\"z\"]"), readStoredData().at("/experiment/tags"));
  }

  @Test
  public void patch_nestedObject_mergedRecursively() {
    patchOk("{\"experiment\": {\"nested\": {\"first\": 1, \"second\": {\"deep\": true}}}}");
    patchOk("{\"experiment\": {\"nested\": {\"second\": {\"other\": \"x\"}}}}");
    assertEquals(
        json("{\"first\": 1, \"second\": {\"deep\": true, \"other\": \"x\"}}"),
        readStoredData().at("/experiment/nested"));
  }

  @Test
  public void patch_emptyObject_changesNothing() {
    val before = readStoredData();
    patchOk("{}");
    assertEquals(before, readStoredData());
  }

  @Test
  public void patch_analysisTypeInPatch_isNotStoredInData() {
    // The merged document is validated with the analysis's existing schema; analysisType is then
    // removed before storing, so it cannot be changed or added to the data this way.
    patchOk("{\"analysisType\": {\"name\": \"sequencingRead\", \"version\": 1}}");
    assertFalse(readStoredData().has("analysisType"));
    assertEquals(
        "variantCall", analysisService.unsecuredDeepRead(analysisId).getAnalysisSchema().getName());
  }

  @Test
  public void patch_removingRequiredField_schemaViolation() {
    endpointTester
        .patchUpdateAnalysisRequestAnd(
            studyId, analysisId, json("{\"experiment\": {\"variantCallingTool\": null}}"))
        .assertServerError(SCHEMA_VIOLATION);
  }

  @Test
  public void patch_wrongType_schemaViolation() {
    endpointTester
        .patchUpdateAnalysisRequestAnd(
            studyId, analysisId, json("{\"experiment\": {\"variantCallingTool\": 123}}"))
        .assertServerError(SCHEMA_VIOLATION);
  }

  @Test
  public void patch_unknownAnalysis_notFound() {
    endpointTester
        .patchUpdateAnalysisRequestAnd(studyId, "no-such-analysis", json("{\"experiment\": {}}"))
        .assertServerError(ANALYSIS_ID_NOT_FOUND);
  }

  @Test
  public void patch_nonObjectPatch_isRejected() {
    // A non-object merge patch replaces the whole document, which then fails schema validation.
    endpointTester
        .patchUpdateAnalysisRequestAnd(studyId, analysisId, json("[1, 2]"))
        .assertServerError(SCHEMA_VIOLATION);
  }
}
