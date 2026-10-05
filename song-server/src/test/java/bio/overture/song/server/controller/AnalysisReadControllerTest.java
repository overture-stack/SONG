package bio.overture.song.server.controller;

import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.web.ResponseOption;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
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
 * Tests the analysis read endpoints: analysis files, id search (GET and POST), and the plain and
 * paginated analysis lists for a study.
 */
@Transactional
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AnalysisReadControllerTest {

  private static final int ANALYSIS_COUNT = 3;

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;

  private EndpointTester endpointTester;
  private String studyId;
  private String otherStudyId;
  private List<Analysis> analyses;

  @BeforeEach
  public void beforeEachTest() {
    this.endpointTester =
        createEndpointTester(webAppContextSetup(webApplicationContext).build(), true);
    val randomGenerator = createRandomGenerator(getClass().getSimpleName());
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    this.studyId = studyGenerator.createRandomStudy();
    this.otherStudyId = studyGenerator.createRandomStudy();
    val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
    this.analyses = new ArrayList<>();
    for (var count = 0; count < ANALYSIS_COUNT; count++) {
      analyses.add(analysisGenerator.createDefaultRandomVariantCallAnalysis());
    }
  }

  @SneakyThrows
  private static JsonNode readBody(ResponseOption response) {
    return readTree(response.assertOk().getResponse().getBody());
  }

  private static List<String> analysisIdsOf(JsonNode analysisArray) {
    return StreamSupport.stream(analysisArray.spliterator(), false)
        .map(analysis -> analysis.path("analysisId").textValue())
        .collect(toList());
  }

  private ResponseOption getAnd(String formattedEndpoint, Object... arguments) {
    return endpointTester.initWebRequest().endpoint(formattedEndpoint, arguments).getAnd();
  }

  private Set<String> allAnalysisIds() {
    return analyses.stream().map(Analysis::getAnalysisId).collect(toSet());
  }

  @Test
  public void getFiles_returnsAllFilesOfTheAnalysis() {
    val analysis = analyses.get(0);
    val files =
        getAnd("studies/%s/analysis/%s/files", studyId, analysis.getAnalysisId())
            .extractManyEntities(FileEntity.class);
    assertEquals(
        analysis.getFiles().stream().map(FileEntity::getObjectId).collect(toSet()),
        files.stream().map(FileEntity::getObjectId).collect(toSet()));
  }

  @Test
  public void getFiles_analysisOfAnotherStudy_notRelated() {
    getAnd("studies/%s/analysis/%s/files", otherStudyId, analyses.get(0).getAnalysisId())
        .assertServerError(ENTITY_NOT_RELATED_TO_STUDY);
  }

  @Test
  public void idSearchGet_exactObjectId_returnsOwningAnalysis() {
    val analysis = analyses.get(1);
    val objectId = analysis.getFiles().get(0).getObjectId();
    val result = readBody(getAnd("studies/%s/analysis/search/id?fileId=%s", studyId, objectId));
    assertEquals(List.of(analysis.getAnalysisId()), analysisIdsOf(result));
  }

  @Test
  public void idSearchGet_noFileId_returnsEveryAnalysisInStudy() {
    val result = readBody(getAnd("studies/%s/analysis/search/id", studyId));
    assertEquals(allAnalysisIds(), Set.copyOf(analysisIdsOf(result)));
  }

  @Test
  public void idSearchGet_noMatch_returnsEmptyList() {
    val result =
        readBody(getAnd("studies/%s/analysis/search/id?fileId=%s", studyId, "no-such-object"));
    assertTrue(result.isArray());
    assertEquals(0, result.size());
  }

  /**
   * POST id search cannot deserialize its request today: IdSearchRequest has a single final field
   * and no creator Jackson can use, so every request is rejected before reaching the controller.
   */
  @Test
  public void idSearchPost_requestCannotBeDeserialized_badRequest() {
    val analysis = analyses.get(2);
    endpointTester
        .initWebRequest()
        .endpoint("studies/%s/analysis/search/id", studyId)
        .body(Map.of("objectId", analysis.getFiles().get(0).getObjectId()))
        .postAnd()
        .assertBadRequest();
  }

  @Test
  public void getAnalyses_defaultStateIsPublished() {
    val result = readBody(getAnd("studies/%s/analysis", studyId));
    assertEquals(0, result.size());
  }

  @Test
  public void getAnalyses_unpublished_returnsAll() {
    val result = readBody(getAnd("studies/%s/analysis?analysisStates=UNPUBLISHED", studyId));
    assertEquals(allAnalysisIds(), Set.copyOf(analysisIdsOf(result)));
  }

  @Test
  public void getAnalysesPaginated_pagesAreSortedByIdAndCountsAreReported() {
    val firstPage =
        readBody(
            getAnd(
                "studies/%s/analysis/paginated?analysisStates=UNPUBLISHED&limit=2&offset=0",
                studyId));
    val secondPage =
        readBody(
            getAnd(
                "studies/%s/analysis/paginated?analysisStates=UNPUBLISHED&limit=2&offset=2",
                studyId));

    assertEquals(ANALYSIS_COUNT, firstPage.path("totalAnalyses").intValue());
    assertEquals(2, firstPage.path("currentTotalAnalyses").intValue());
    assertEquals(ANALYSIS_COUNT, secondPage.path("totalAnalyses").intValue());
    assertEquals(1, secondPage.path("currentTotalAnalyses").intValue());

    val pagedIds = new ArrayList<String>();
    pagedIds.addAll(analysisIdsOf(firstPage.path("analyses")));
    pagedIds.addAll(analysisIdsOf(secondPage.path("analyses")));
    val sortedIds = allAnalysisIds().stream().sorted().collect(toList());
    assertEquals(sortedIds, pagedIds);
  }

  @Test
  public void getAnalysesPaginated_defaultsToPublished() {
    val result = readBody(getAnd("studies/%s/analysis/paginated", studyId));
    assertEquals(0, result.path("totalAnalyses").intValue());
    assertEquals(0, result.path("analyses").size());
  }
}
