package bio.overture.song.server.controller;

import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.MAIN;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.TEST;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_TYPE;
import static bio.overture.song.server.model.enums.ModelAttributeNames.NAME;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY;
import static bio.overture.song.server.model.enums.ModelAttributeNames.VERSION;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.dto.SubmitResponse;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.generator.StudyGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.val;
import org.junit.Before;
import org.springframework.lang.Nullable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public abstract class AbstractEnforcedTester {

  private static final ResourceFetcher DOCUMENTS_FETCHER =
      ResourceFetcher.builder().resourceType(TEST).dataDir(Paths.get("documents/")).build();

  private static final ResourceFetcher LEGACY_SCHEMA_FETCHER =
      ResourceFetcher.builder()
          .resourceType(MAIN)
          .dataDir(Paths.get("schemas/analysis/legacy/"))
          .build();

  // This was done because the autowired mockMvc wasn't working properly, it was getting http 403
  // errors
  protected abstract WebApplicationContext getWebApplicationContext();

  protected abstract StudyService getStudyService();

  /** State */
  private RandomGenerator randomGenerator;

  @Getter private EndpointTester endpointTester;
  private MockMvc mockMvc;

  @Getter private AnalysisType latestAnalysisType;

  @Getter private String studyId;
  private boolean initialized;
  private JsonNode variantCallSchema;

  @Before
  public void beforeEachTest() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(getWebApplicationContext()).build();
    this.randomGenerator = createRandomGenerator(getClass().getSimpleName());
    this.endpointTester = createEndpointTester(mockMvc, true);
    lazyInit();
  }

  private void lazyInit() {
    if (!initialized) {
      initialRegistration();
      registerAgain();
      initialized = true;
    }
  }

  private void initialRegistration() {
    val studyGenerator = StudyGenerator.createStudyGenerator(getStudyService(), randomGenerator);
    studyId = studyGenerator.createRandomStudy();
    variantCallSchema = LEGACY_SCHEMA_FETCHER.readJsonNode("variantCall.json");
    val analysisTypeVersion =
        endpointTester
            .registerAnalysisTypePostRequestAnd(
                RegisterAnalysisTypeRequest.builder()
                    .name(randomGenerator.generateRandomAsciiString(7))
                    .schema(variantCallSchema)
                    .build())
            .extractOneEntity(AnalysisType.class);
    assertEquals(analysisTypeVersion.getVersion().intValue(), 1);
    latestAnalysisType = analysisTypeVersion;
  }

  protected void registerAgain() {
    assertFalse(isNull(latestAnalysisType));
    val analysisTypeVersion =
        endpointTester
            .registerAnalysisTypePostRequestAnd(
                RegisterAnalysisTypeRequest.builder()
                    .name(latestAnalysisType.getName())
                    .schema(variantCallSchema)
                    .build())
            .extractOneEntity(AnalysisType.class);
    assertEquals(analysisTypeVersion.getName(), latestAnalysisType.getName());
    assertEquals(analysisTypeVersion.getVersion().intValue(), latestAnalysisType.getVersion() + 1);
    latestAnalysisType = analysisTypeVersion;
  }

  protected JsonNode buildTestEnforcePayload(@Nullable Boolean isLatestVersion) {
    val j = (ObjectNode) DOCUMENTS_FETCHER.readJsonNode("variantcall-valid.json");
    j.put(STUDY, studyId);
    val analysisTypeNode = (ObjectNode) j.path(ANALYSIS_TYPE);
    analysisTypeNode.put(NAME, latestAnalysisType.getName());
    if (isNull(isLatestVersion)) {
      analysisTypeNode.remove(VERSION);
    } else if (isLatestVersion) {
      analysisTypeNode.put(VERSION, latestAnalysisType.getVersion());
    } else {
      analysisTypeNode.put(VERSION, latestAnalysisType.getVersion() - 1);
    }
    return j;
  }

  protected SubmitResponse submit(boolean isLatestVersion) {
    // Create a valid payload containing the latest version
    val payload = buildTestEnforcePayload(isLatestVersion);
    return getEndpointTester()
        .submitPostRequestAnd(getStudyId(), payload)
        .extractOneEntity(SubmitResponse.class);
  }
}
