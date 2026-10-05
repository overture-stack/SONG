package bio.overture.song.server.contract;

import static bio.overture.song.server.utils.contract.GoldenFiles.assertMatchesGolden;

import bio.overture.song.server.utils.contract.JsonNormalizer;
import bio.overture.song.server.utils.contract.RawHttpClient;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;

/**
 * Locks the JSON returned by each read endpoint with golden files under {@code golden/http}.
 *
 * <p>Study {@code CONTRACT-READ} holds one published and one unpublished analysis. Lists are
 * filtered so they contain a single generated analysis, because generated ids are random and would
 * make the order of a longer list change between runs. The seeded analyses {@code AN1}/{@code AN2}
 * have no analysis data, which makes the analysis list endpoints fail, so they are only used
 * through the file-based legacy entity endpoints.
 */
public class HttpContractTest extends AbstractContractTest {

  private static final String STUDY_ID = "CONTRACT-READ";
  private static final String SEEDED_STUDY_ID = "ABC123";

  @LocalManagementPort private int managementPort;

  private String publishedAnalysisId;
  private String unpublishedAnalysisId;

  @BeforeAll
  public void setUpData() {
    createStudy(STUDY_ID);
    publishedAnalysisId = submit(STUDY_ID, readVariantCallPayload(STUDY_ID));
    publish(STUDY_ID, publishedAnalysisId);
    unpublishedAnalysisId = submit(STUDY_ID, readVariantCallPayload(STUDY_ID));
  }

  private JsonNormalizer createAnalysisNormalizer() {
    val normalizer = createNormalizer();
    withAnalysisIds(normalizer, "published", STUDY_ID, publishedAnalysisId);
    withAnalysisIds(normalizer, "unpublished", STUDY_ID, unpublishedAnalysisId);
    return normalizer;
  }

  @Test
  public void getAnalysisById() {
    assertGolden(
        "http/get-analysis",
        client.get("/studies/" + STUDY_ID + "/analysis/" + publishedAnalysisId),
        createAnalysisNormalizer());
  }

  @Test
  public void getAnalysesForStudy_defaultState() {
    assertGolden(
        "http/get-analyses-for-study-default-state",
        client.get("/studies/" + STUDY_ID + "/analysis"),
        createAnalysisNormalizer());
  }

  @Test
  public void getAnalysesForStudy_unpublished() {
    assertGolden(
        "http/get-analyses-for-study-unpublished",
        client.get("/studies/" + STUDY_ID + "/analysis?analysisStates=UNPUBLISHED"),
        createAnalysisNormalizer());
  }

  @Test
  public void getAnalysesPaginated() {
    assertGolden(
        "http/get-analyses-paginated",
        client.get(
            "/studies/" + STUDY_ID + "/analysis/paginated?analysisStates=UNPUBLISHED&limit=1"),
        createAnalysisNormalizer());
  }

  @Test
  public void getAnalysesPaginated_offsetPastEnd() {
    assertGolden(
        "http/get-analyses-paginated-offset-past-end",
        client.get(
            "/studies/"
                + STUDY_ID
                + "/analysis/paginated?analysisStates=PUBLISHED,UNPUBLISHED&limit=1&offset=5"),
        createAnalysisNormalizer());
  }

  @Test
  public void getAnalysisFiles() {
    assertGolden(
        "http/get-analysis-files",
        client.get("/studies/" + STUDY_ID + "/analysis/" + publishedAnalysisId + "/files"),
        createAnalysisNormalizer());
  }

  @Test
  public void idSearch_get() {
    assertGolden(
        "http/id-search-get",
        client.get(
            "/studies/"
                + STUDY_ID
                + "/analysis/search/id?fileId="
                + readVcfObjectId(STUDY_ID, publishedAnalysisId)),
        createAnalysisNormalizer());
  }

  @Test
  public void idSearch_post() {
    val request =
        MAPPER.createObjectNode().put("objectId", readVcfObjectId(STUDY_ID, publishedAnalysisId));
    assertGolden(
        "http/id-search-post",
        client.post("/studies/" + STUDY_ID + "/analysis/search/id", request.toString()),
        createAnalysisNormalizer());
  }

  @Test
  public void getStudy() {
    assertGolden("http/get-study", client.get("/studies/" + STUDY_ID), createNormalizer());
  }

  @Test
  public void getAllStudies() {
    assertGolden("http/get-all-studies", client.get("/studies/all"), createNormalizer());
  }

  @Test
  public void getFile() {
    assertGolden(
        "http/get-file",
        client.get(
            "/studies/" + STUDY_ID + "/files/" + readVcfObjectId(STUDY_ID, publishedAnalysisId)),
        createAnalysisNormalizer());
  }

  @Test
  public void exportStudy() {
    // Both analyses are exported; their payloads are identical because exported payloads carry no
    // ids, so the order does not affect the comparison.
    assertGolden(
        "http/export-study", client.get("/export/studies/" + STUDY_ID), createAnalysisNormalizer());
  }

  @Test
  public void exportAnalyses() {
    assertGolden(
        "http/export-analyses",
        client.get("/export/analysis/" + publishedAnalysisId),
        createAnalysisNormalizer());
  }

  @Test
  public void listSchemas() {
    assertGolden("http/list-schemas", client.get("/schemas?limit=5&offset=0"), createNormalizer());
  }

  @Test
  public void listSchemas_hideSchema() {
    assertGolden(
        "http/list-schemas-hide-schema",
        client.get("/schemas?hideSchema=true&sort=version&sortOrder=ASC"),
        createNormalizer());
  }

  @Test
  public void getSchema() {
    assertGolden("http/get-schema", client.get("/schemas/variantCall"), createNormalizer());
  }

  @Test
  public void getSchema_unrenderedVersion() {
    assertGolden(
        "http/get-schema-unrendered-version",
        client.get("/schemas/variantCall?version=1&unrenderedOnly=true"),
        createNormalizer());
  }

  @Test
  public void getRegistrationSchema() {
    assertGolden(
        "http/get-registration-schema", client.get("/schemas/registration"), createNormalizer());
  }

  @Test
  public void findLegacyEntities() {
    // The page JSON is Spring Data's PageImpl serialized directly, so this locks its shape.
    assertGolden(
        "http/find-legacy-entities",
        client.get("/entities?projectCode=" + SEEDED_STUDY_ID + "&size=2&page=1"),
        createNormalizer());
  }

  @Test
  public void findLegacyEntities_fieldFilter() {
    assertGolden(
        "http/find-legacy-entities-field-filter",
        client.get(
            "/entities?projectCode=" + SEEDED_STUDY_ID + "&fields=id&fields=fileName&size=2"),
        createNormalizer());
  }

  @Test
  public void getLegacyEntity() {
    assertGolden("http/get-legacy-entity", client.get("/entities/FI1"), createNormalizer());
  }

  /** Actuator runs on the management port; recorded here for the profile without security. */
  @Test
  public void actuatorStatuses() {
    val managementClient = new RawHttpClient("http://localhost:" + managementPort);
    val statuses = MAPPER.createObjectNode();
    for (val path : List.of("/actuator", "/actuator/health", "/actuator/info", "/actuator/env")) {
      statuses.put("GET " + path, managementClient.get(path).getStatus());
    }
    assertMatchesGolden("http/actuator-statuses", statuses);
  }

  @Test
  public void isAlive() {
    assertGolden("http/is-alive", client.get("/isAlive"), createNormalizer());
  }

  /**
   * Records the status of each GET route when called with a trailing slash. Spring MVC 6 no longer
   * matches these by default, so this shows which clients a change would affect.
   */
  @Test
  public void trailingSlashStatuses() {
    val objectId = readVcfObjectId(STUDY_ID, publishedAnalysisId);
    val paths =
        new String[] {
          "/isAlive/",
          "/studies/" + STUDY_ID + "/",
          "/studies/all/",
          "/studies/" + STUDY_ID + "/analysis/",
          "/studies/" + STUDY_ID + "/analysis/paginated/",
          "/studies/" + STUDY_ID + "/analysis/" + publishedAnalysisId + "/",
          "/studies/" + STUDY_ID + "/analysis/" + publishedAnalysisId + "/files/",
          "/studies/" + STUDY_ID + "/analysis/search/id/",
          "/studies/" + STUDY_ID + "/files/" + objectId + "/",
          "/export/studies/" + STUDY_ID + "/",
          "/export/analysis/" + publishedAnalysisId + "/",
          "/schemas/",
          "/schemas/variantCall/",
          "/schemas/registration/",
          "/entities/",
          "/entities/FI1/"
        };
    val normalizer = createAnalysisNormalizer();
    val statuses = MAPPER.createObjectNode();
    for (val path : paths) {
      statuses.put("GET " + normalizer.normalizeText(path), client.get(path).getStatus());
    }
    assertMatchesGolden("http/trailing-slash-statuses", statuses);
  }
}
