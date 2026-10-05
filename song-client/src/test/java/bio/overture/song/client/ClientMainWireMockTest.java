package bio.overture.song.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import bio.overture.song.client.cli.ClientMain;
import bio.overture.song.client.config.Config;
import bio.overture.song.client.config.CustomRestClientConfig;
import bio.overture.song.sdk.config.impl.DefaultRetryConfig;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Runs the {@code sing} CLI through the real SDK (Endpoint URL building, DefaultRestClient, retry
 * decorator and ServerResponseErrorHandler) against a WireMock server standing in for Song.
 *
 * <p>Checks the request each command sends (method, path, query, headers, body) and the exit code
 * and console output for successful and failed responses.
 */
public class ClientMainWireMockTest extends AbstractClientMainTest {

  private static final String STUDY_ID = "ABC123";
  private static final String ACCESS_TOKEN = "cli-test-token";
  private static final String ANALYSIS_ID = "an-analysis-id";
  private static final String JSON_WITH_CHARSET = "application/json;charset=UTF-8";

  @Rule public WireMockRule songServer = new WireMockRule(options().dynamicPort());
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  /** Overrides the server URL for tests that need a server that is not running. */
  private String serverUrlOverride;

  @Override
  protected ClientMain getClientMain() {
    val serverUrl =
        serverUrlOverride != null ? serverUrlOverride : "http://localhost:" + songServer.port();
    val config =
        Config.builder()
            .client(
                CustomRestClientConfig.builder()
                    .serverUrl(serverUrl)
                    .studyId(STUDY_ID)
                    .accessToken(ACCESS_TOKEN)
                    .programName("sing")
                    .debug(false)
                    .build())
            .retry(
                DefaultRetryConfig.builder()
                    .maxRetries(3)
                    .initialBackoff(10L)
                    .multiplier(1.0)
                    .build())
            .build();
    return ClientMain.createClientMain(config);
  }

  private static String songErrorBody(String errorId, int status, String statusName) {
    return "{\"errorId\": \""
        + errorId
        + "\", \"httpStatusCode\": "
        + status
        + ", \"httpStatusName\": \""
        + statusName
        + "\", \"requestUrl\": \"http://song/request\", \"datetime\": \"2024-01-01T00:00:00Z\","
        + " \"timestamp\": 1704067200000, \"message\": \"A test error\", \"debugMessage\": \"N/A\","
        + " \"stackTrace\": []}";
  }

  @Test
  public void ping_sendsAuthorizationAndJsonHeaders() {
    songServer.stubFor(get(urlEqualTo("/isAlive")).willReturn(aResponse().withBody("true")));

    val output = executeMain("ping");

    assertEquals(0, getExitCode().intValue());
    assertTrue(output.getOut(), output.getOut().contains("true"));
    songServer.verify(
        getRequestedFor(urlEqualTo("/isAlive"))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
            .withHeader("Accept", equalTo(JSON_WITH_CHARSET)));
  }

  @Test
  @SneakyThrows
  public void submit_postsFileContentToStudy() {
    val payload = "{\"studyId\": \"ABC123\", \"analysisType\": {\"name\": \"variantCall\"}}";
    val payloadFile = temporaryFolder.newFile("payload.json").toPath();
    Files.writeString(payloadFile, payload, StandardCharsets.UTF_8);
    songServer.stubFor(
        post(urlEqualTo("/submit/" + STUDY_ID + "?allowDuplicates=false"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"analysisId\": \"" + ANALYSIS_ID + "\", \"status\": \"OK\"}")));

    val output = executeMain("submit", "-f", payloadFile.toString());

    assertEquals(0, getExitCode().intValue());
    assertTrue(output.getOut(), output.getOut().contains(ANALYSIS_ID));
    songServer.verify(
        postRequestedFor(urlEqualTo("/submit/" + STUDY_ID + "?allowDuplicates=false"))
            .withHeader("Content-Type", equalTo(JSON_WITH_CHARSET))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
            .withRequestBody(equalToJson(payload)));
  }

  @Test
  public void search_byAnalysisId_getsAnalysis() {
    songServer.stubFor(
        get(urlEqualTo("/studies/" + STUDY_ID + "/analysis/" + ANALYSIS_ID))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"analysisId\": \""
                            + ANALYSIS_ID
                            + "\", \"studyId\": \"ABC123\", \"analysisState\": \"PUBLISHED\"}")));

    val output = executeMain("search", "-a", ANALYSIS_ID);

    assertEquals(0, getExitCode().intValue());
    assertTrue(output.getOut(), output.getOut().contains("\"analysisState\" : \"PUBLISHED\""));
  }

  @Test
  public void search_byFileId_usesIdSearchQuery() {
    songServer.stubFor(
        get(urlPathEqualTo("/studies/" + STUDY_ID + "/analysis/search/id"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[]")));

    executeMain("search", "-f", "an-object-id");

    assertEquals(0, getExitCode().intValue());
    songServer.verify(
        getRequestedFor(
            urlEqualTo("/studies/" + STUDY_ID + "/analysis/search/id?fileId=an-object-id")));
  }

  @Test
  public void publish_sendsIgnoreUndefinedMd5Flag() {
    val path = "/studies/" + STUDY_ID + "/analysis/publish/" + ANALYSIS_ID;
    songServer.stubFor(
        put(urlPathEqualTo(path))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"message\": \"AnalysisId "
                            + ANALYSIS_ID
                            + " successfully published\"}")));

    val output = executeMain("publish", "-a", ANALYSIS_ID, "-i");

    assertEquals(0, getExitCode().intValue());
    assertTrue(output.getOut(), output.getOut().contains("successfully published"));
    songServer.verify(putRequestedFor(urlEqualTo(path + "?ignoreUndefinedMd5=true")));
  }

  @Test
  public void listAnalysisTypes_buildsQueryParameters() {
    songServer.stubFor(
        get(urlPathEqualTo("/schemas"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"limit\": 2, \"offset\": 1, \"count\": 0, \"resultSet\": []}")));

    executeMain("list-analysis-types", "-n", "variantCall", "-v", "1", "-l", "2", "-o", "1", "-hs");

    assertEquals(0, getExitCode().intValue());
    val requests = songServer.findAll(getRequestedFor(urlPathEqualTo("/schemas")));
    assertEquals(1, requests.size());
    val url = requests.get(0).getUrl();
    for (val expectedParameter :
        new String[] {
          "names=variantCall", "versions=1", "limit=2", "offset=1", "hideSchema=true"
        }) {
      assertTrue(url, url.contains(expectedParameter));
    }
  }

  @Test
  public void serverError4xx_exitCodeOneAndSongErrorOnStderr() {
    songServer.stubFor(
        get(urlEqualTo("/studies/" + STUDY_ID + "/analysis/" + ANALYSIS_ID))
            .willReturn(
                aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody(songErrorBody("analysis.id.not.found", 404, "NOT_FOUND"))));

    val output = executeMain("search", "-a", ANALYSIS_ID);

    assertEquals(1, getExitCode().intValue());
    assertTrue(output.getErr(), output.getErr().contains("analysis.id.not.found"));
    // A Song error response is not retried.
    songServer.verify(
        1, getRequestedFor(urlEqualTo("/studies/" + STUDY_ID + "/analysis/" + ANALYSIS_ID)));
  }

  @Test
  public void serverError5xx_exitCodeOneAndNotRetried() {
    songServer.stubFor(
        get(urlEqualTo("/studies/" + STUDY_ID + "/analysis/" + ANALYSIS_ID))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody(songErrorBody("unknown.error", 500, "INTERNAL_SERVER_ERROR"))));

    val output = executeMain("search", "-a", ANALYSIS_ID);

    assertEquals(1, getExitCode().intValue());
    assertTrue(output.getErr(), output.getErr().contains("unknown.error"));
    songServer.verify(
        1, getRequestedFor(urlEqualTo("/studies/" + STUDY_ID + "/analysis/" + ANALYSIS_ID)));
  }

  @Test
  public void invalidTokenResponse_reportedAsUnauthorizedToken() {
    songServer.stubFor(
        get(urlEqualTo("/studies/" + STUDY_ID + "/analysis/" + ANALYSIS_ID))
            .willReturn(
                aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"error\": \"invalid_token\", \"error_description\": \"expired\"}")));

    val output = executeMain("search", "-a", ANALYSIS_ID);

    assertEquals(1, getExitCode().intValue());
    assertTrue(output.getErr(), output.getErr().contains("unauthorized.token"));
  }

  /** A 401 without a body, as the secure profile sends it, crashes the error handler. */
  @Test
  public void unauthorizedWithoutBody_reportedAsUnknownError() {
    songServer.stubFor(get(urlEqualTo("/isAlive")).willReturn(aResponse().withStatus(401)));

    val output = executeMain("ping");

    assertEquals(1, getExitCode().intValue());
    assertTrue(output.getErr(), output.getErr().contains("Unknown error[NullPointerException"));
  }

  /**
   * A connection reset is not retried: the retry listener only retries connection timeouts and 503
   * responses, and the error handler turns every error response into a ServerException first. The
   * CLI then reports an unknown NullPointerException rather than a connection error.
   */
  @Test
  public void connectionReset_notRetriedAndReportedAsUnknownError() {
    songServer.stubFor(
        get(urlEqualTo("/isAlive"))
            .inScenario("flaky")
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
            .willSetStateTo("recovered"));
    songServer.stubFor(
        get(urlEqualTo("/isAlive"))
            .inScenario("flaky")
            .whenScenarioStateIs("recovered")
            .willReturn(aResponse().withBody("true")));

    val output = executeMain("ping");

    assertEquals(1, getExitCode().intValue());
    assertTrue(output.getErr(), output.getErr().contains("Unknown error[NullPointerException"));
    songServer.verify(1, getRequestedFor(urlEqualTo("/isAlive")));
  }

  @Test
  public void serviceUnavailable_notRetried() {
    songServer.stubFor(
        get(urlEqualTo("/isAlive"))
            .willReturn(
                aResponse()
                    .withStatus(503)
                    .withHeader("Content-Type", "application/json")
                    .withBody(songErrorBody("unknown.error", 503, "SERVICE_UNAVAILABLE"))));

    val output = executeMain("ping");

    assertEquals(1, getExitCode().intValue());
    assertTrue(output.getErr(), output.getErr().contains("unknown.error"));
    songServer.verify(1, getRequestedFor(urlEqualTo("/isAlive")));
  }

  @Test
  @SneakyThrows
  public void serverNotRunning_reportsServerMayNotBeRunning() {
    // Reserve a port with nothing listening on it.
    int unusedPort;
    try (val socket = new ServerSocket(0)) {
      unusedPort = socket.getLocalPort();
    }
    serverUrlOverride = "http://localhost:" + unusedPort;

    val output = executeMain("search", "-a", ANALYSIS_ID);

    assertEquals(1, getExitCode().intValue());
    assertTrue(output.getErr(), output.getErr().contains("may not be running"));
  }
}
