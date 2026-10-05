package bio.overture.song.server.controller;

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.repository.AnalysisRepository;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.web.ResponseOption;
import com.fasterxml.jackson.databind.JsonNode;
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

/** Tests GET /export/studies/{studyId} and GET /export/analysis/{analysisIds}. */
@Transactional
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ExportControllerTest {

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;
  @Autowired private AnalysisRepository analysisRepository;

  private EndpointTester endpointTester;
  private String studyId;
  private Analysis publishedAnalysis;
  private Analysis unpublishedAnalysis;

  @BeforeEach
  public void beforeEachTest() {
    this.endpointTester =
        createEndpointTester(webAppContextSetup(webApplicationContext).build(), true);
    val randomGenerator = createRandomGenerator(getClass().getSimpleName());
    this.studyId = createStudyGenerator(studyService, randomGenerator).createRandomStudy();
    val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
    this.unpublishedAnalysis = analysisGenerator.createDefaultRandomVariantCallAnalysis();
    this.publishedAnalysis = analysisGenerator.createDefaultRandomVariantCallAnalysis();

    // Publishing through the service needs Score, so the state is set directly. Export ignores
    // the state; the two states show that.
    val stored = analysisRepository.findById(publishedAnalysis.getAnalysisId()).get();
    stored.setAnalysisState(PUBLISHED.name());
    analysisRepository.save(stored);
  }

  @SneakyThrows
  private static JsonNode readBody(ResponseOption response) {
    return readTree(response.assertOk().getResponse().getBody());
  }

  private ResponseOption getAnd(String formattedEndpoint, Object... arguments) {
    return endpointTester.initWebRequest().endpoint(formattedEndpoint, arguments).getAnd();
  }

  @Test
  public void exportStudy_everyAnalysisStateIsExported() {
    val result = readBody(getAnd("export/studies/%s", studyId));
    assertEquals(1, result.size());
    assertEquals(studyId, result.get(0).path("studyId").textValue());
    val payloads = result.get(0).path("payloads");
    assertEquals(2, payloads.size());
    for (val payload : payloads) {
      assertEquals(publishedAnalysis.getFiles().size(), payload.path("files").size());
    }
  }

  @Test
  public void exportStudy_unknownStudy_notFound() {
    getAnd("export/studies/%s", "NO-SUCH-STUDY").assertServerError(STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void exportAnalyses_commaSeparatedIds_exportsEachRegardlessOfState() {
    val result =
        readBody(
            getAnd(
                "export/analysis/%s,%s",
                publishedAnalysis.getAnalysisId(), unpublishedAnalysis.getAnalysisId()));
    assertEquals(1, result.size());
    assertEquals(studyId, result.get(0).path("studyId").textValue());
    assertEquals(2, result.get(0).path("payloads").size());
  }

  @Test
  public void exportAnalyses_unknownId_notFound() {
    getAnd("export/analysis/%s", "no-such-analysis").assertServerError(ANALYSIS_ID_NOT_FOUND);
  }
}
