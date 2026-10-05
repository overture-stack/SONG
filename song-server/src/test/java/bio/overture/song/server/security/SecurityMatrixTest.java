package bio.overture.song.server.security;

import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.server.utils.contract.GoldenFiles.assertMatchesGolden;
import static bio.overture.song.server.utils.contract.RawHttpClient.APPLICATION_JSON;
import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;

import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.service.ExportService;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.utils.contract.RawHttpClient;
import bio.overture.song.server.utils.contract.RawResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

/**
 * Locks which requests the {@code secure} profile accepts, with golden files under {@code
 * golden/security}. Requests go over real HTTP, so the whole servlet filter chain and Tomcat error
 * dispatches are included.
 *
 * <p>Uses the {@code ego} provider, where scopes are read from the JWT's {@code context.scope}
 * claim. The Keycloak provider is covered by {@link KeycloakSecurityContractTest}.
 */
@TestPropertySource(properties = "auth.server.provider=ego")
public class SecurityMatrixTest extends AbstractSecurityContractTest {

  private static final String STUDY_ID = "SECURE-STUDY";
  private static final String OTHER_STUDY_ID = "OTHER-STUDY";

  /** Export of this study fails with a RuntimeException, to produce a 500 on a permitted route. */
  private static final String FAILING_EXPORT_STUDY_ID = "FORCED-500";

  @Autowired private AnalysisService analysisService;
  @Autowired private StudyService studyService;
  @SpyBean private ExportService exportService;
  @LocalManagementPort private int managementPort;

  private int createdStudyCount = 0;

  @BeforeAll
  public void setUpData() {
    for (val studyId : List.of(STUDY_ID, OTHER_STUDY_ID)) {
      studyService.saveStudy(Study.builder().studyId(studyId).name(studyId).build());
    }
  }

  /** A request to a secured endpoint, and the study whose scope authorizes it. */
  @Value
  private static class SecuredRequest {
    String method;
    String path;
    String body;
    String studyId;
  }

  private String createAnalysis() {
    val payload = readVariantCallPayload(STUDY_ID);
    return analysisService.create(STUDY_ID, toPayload(payload)).getAnalysisId();
  }

  private static Payload toPayload(ObjectNode payload) {
    return fromJson(payload, Payload.class);
  }

  private String createPublishableAnalysis() {
    val analysisId = createAnalysis();
    stubStorageWithUploadedFiles(STUDY_ID, analysisId);
    return analysisId;
  }

  private String firstObjectId(String analysisId) {
    return analysisService.unsecuredReadFiles(analysisId).get(0).getObjectId();
  }

  /**
   * Every endpoint guarded by {@code @PreAuthorize}, keyed by a stable name. Each supplier creates
   * fresh data, so every token case gets a request that would succeed once authorized.
   */
  private Map<String, Supplier<SecuredRequest>> securedEndpoints() {
    val endpoints = new LinkedHashMap<String, Supplier<SecuredRequest>>();
    endpoints.put(
        "PUT /studies/{studyId}/analysis/{analysisId}",
        () -> {
          val update = MAPPER.createObjectNode();
          update.putObject("analysisType").put("name", "variantCall").put("version", 1);
          update
              .putObject("experiment")
              .put("variantCallingTool", "updated")
              .put("matchedNormalSampleSubmitterId", "sample-x24-11a");
          return new SecuredRequest(
              "PUT",
              "/studies/" + STUDY_ID + "/analysis/" + createAnalysis(),
              update.toString(),
              STUDY_ID);
        });
    endpoints.put(
        "PATCH /studies/{studyId}/analysis/{analysisId}",
        () ->
            new SecuredRequest(
                "PATCH",
                "/studies/" + STUDY_ID + "/analysis/" + createAnalysis(),
                "{\"experiment\": {\"variantCallingTool\": \"patched\"}}",
                STUDY_ID));
    endpoints.put(
        "PUT /studies/{studyId}/analysis/publish/{analysisId}",
        () ->
            new SecuredRequest(
                "PUT",
                "/studies/" + STUDY_ID + "/analysis/publish/" + createPublishableAnalysis(),
                null,
                STUDY_ID));
    endpoints.put(
        "PUT /studies/{studyId}/analysis/unpublish/{analysisId}",
        () ->
            new SecuredRequest(
                "PUT",
                "/studies/" + STUDY_ID + "/analysis/unpublish/" + createAnalysis(),
                null,
                STUDY_ID));
    endpoints.put(
        "PUT /studies/{studyId}/analysis/suppress/{analysisId}",
        () ->
            new SecuredRequest(
                "PUT",
                "/studies/" + STUDY_ID + "/analysis/suppress/" + createAnalysis(),
                null,
                STUDY_ID));
    endpoints.put(
        "PUT /studies/{studyId}/files/{objectId}",
        () ->
            new SecuredRequest(
                "PUT",
                "/studies/" + STUDY_ID + "/files/" + firstObjectId(createAnalysis()),
                "{\"fileMd5sum\": \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}",
                STUDY_ID));
    endpoints.put(
        "DELETE /studies/{studyId}/files/{objectIds}",
        () ->
            new SecuredRequest(
                "DELETE",
                "/studies/" + STUDY_ID + "/files/" + firstObjectId(createAnalysis()),
                null,
                STUDY_ID));
    endpoints.put(
        "POST /studies/{studyId}/",
        () -> {
          createdStudyCount++;
          val newStudyId = "NEW-STUDY-" + createdStudyCount;
          val study = MAPPER.createObjectNode().put("studyId", newStudyId).put("name", "new");
          return new SecuredRequest(
              "POST", "/studies/" + newStudyId + "/", study.toString(), newStudyId);
        });
    endpoints.put(
        "POST /submit/{studyId}",
        () ->
            new SecuredRequest(
                "POST",
                "/submit/" + STUDY_ID + "?allowDuplicates=true",
                readVariantCallPayload(STUDY_ID).toString(),
                STUDY_ID));
    endpoints.put(
        "POST /schemas",
        () -> {
          val request = MAPPER.createObjectNode().put("name", "securedType");
          request
              .putObject("schema")
              .put("type", "object")
              .putObject("properties")
              .putObject("note")
              .put("type", "string");
          return new SecuredRequest("POST", "/schemas", request.toString(), STUDY_ID);
        });
    return endpoints;
  }

  /** Bearer tokens to try, keyed by name; a null token sends no Authorization header. */
  private Map<String, Function<SecuredRequest, String>> tokenCases() {
    val tokenCases = new LinkedHashMap<String, Function<SecuredRequest, String>>();
    tokenCases.put("noToken", request -> null);
    tokenCases.put("malformedToken", request -> "not-a-jwt");
    tokenCases.put("expiredSystemScope", request -> jwt(List.of(systemScope()), true));
    tokenCases.put("noScopes", request -> jwt(List.of(), false));
    tokenCases.put("otherStudyScope", request -> jwt(List.of(studyScope(OTHER_STUDY_ID)), false));
    tokenCases.put("studyScope", request -> jwt(List.of(studyScope(request.getStudyId())), false));
    tokenCases.put("systemScope", request -> jwt(List.of(systemScope()), false));
    return tokenCases;
  }

  private RawResponse send(SecuredRequest request, String token) {
    return client
        .withBearerToken(token)
        .send(
            request.getMethod(),
            request.getPath(),
            isNull(request.getBody()) ? null : APPLICATION_JSON,
            request.getBody());
  }

  @Test
  public void securedEndpoints_statusPerToken() {
    val statuses = MAPPER.createObjectNode();
    securedEndpoints()
        .forEach(
            (endpointName, requestSupplier) -> {
              val endpointStatuses = statuses.putObject(endpointName);
              tokenCases()
                  .forEach(
                      (tokenName, tokenFunction) -> {
                        val request = requestSupplier.get();
                        endpointStatuses.put(
                            tokenName, send(request, tokenFunction.apply(request)).getStatus());
                      });
            });
    assertMatchesGolden("security/secured-endpoints", statuses);
  }

  @Test
  public void unauthorizedAndForbiddenBodies() {
    val request = securedEndpoints().get("POST /submit/{studyId}").get();
    val normalizer = createNormalizer().ignoreField("stackTrace").ignoreField("timestamp");
    assertGolden("security/no-token", send(request, null), normalizer);
    assertGolden(
        "security/expired-token", send(request, jwt(List.of(systemScope()), true)), normalizer);
    assertGolden(
        "security/forbidden",
        send(request, jwt(List.of(studyScope(OTHER_STUDY_ID)), false)),
        normalizer);
  }

  /** Every route the filter chain permits, called without a token. */
  @Test
  public void permittedRoutes_statusWithoutToken() {
    val analysisId = createAnalysis();
    val objectId = firstObjectId(analysisId);
    val paths =
        List.of(
            "/isAlive",
            "/entities",
            "/entities/FI1",
            "/export/studies/" + STUDY_ID,
            "/export/analysis/" + analysisId,
            "/schemas",
            "/schemas/variantCall",
            "/schemas/registration",
            "/studies/all",
            "/studies/" + STUDY_ID,
            "/studies/" + STUDY_ID + "/analysis?analysisStates=UNPUBLISHED",
            "/studies/" + STUDY_ID + "/analysis/paginated?analysisStates=UNPUBLISHED",
            "/studies/" + STUDY_ID + "/analysis/" + analysisId,
            "/studies/" + STUDY_ID + "/analysis/" + analysisId + "/files",
            "/studies/" + STUDY_ID + "/analysis/search/id?fileId=" + objectId,
            "/studies/" + STUDY_ID + "/files/" + objectId,
            "/swagger-ui.html",
            "/v2/api-docs",
            "/api-docs");
    val normalizer =
        createNormalizer()
            .replaceValue(analysisId, "analysisId")
            .replaceValue(objectId, "objectId");
    val statuses = MAPPER.createObjectNode();
    for (val path : paths) {
      statuses.put("GET " + normalizer.normalizeText(path), client.get(path).getStatus());
    }
    assertMatchesGolden("security/permitted-routes", statuses);
  }

  /**
   * A bearer token is validated whenever one is sent, even on a route that needs none, so the CLI
   * cannot reach {@code /isAlive} with an invalid token.
   */
  @Test
  public void permittedRoutes_statusWithTokens() {
    val statuses = MAPPER.createObjectNode();
    statuses.put(
        "GET /isAlive malformedToken",
        client.withBearerToken("not-a-jwt").get("/isAlive").getStatus());
    statuses.put(
        "GET /isAlive expiredToken",
        client.withBearerToken(jwt(List.of(systemScope()), true)).get("/isAlive").getStatus());
    statuses.put(
        "GET /isAlive validTokenNoScopes",
        client.withBearerToken(jwt(List.of(), false)).get("/isAlive").getStatus());
    val unknownApiKey = UUID.randomUUID().toString();
    statuses.put(
        "GET /isAlive unknownApiKey",
        client.withBearerToken(unknownApiKey).get("/isAlive").getStatus());
    assertMatchesGolden("security/permitted-routes-with-tokens", statuses);
  }

  /**
   * Errors on permitted routes must keep their status for anonymous callers rather than turning
   * into 401 when the error is dispatched to {@code /error}.
   */
  @Test
  public void permittedRoutes_anonymousErrors() {
    // Spy stubs are reset after each test, so the failure is stubbed here.
    doThrow(new RuntimeException("Forced failure"))
        .when(exportService)
        .exportPayloadsForStudy(FAILING_EXPORT_STUDY_ID);
    val normalizer = createNormalizer().ignoreField("stackTrace").ignoreField("timestamp");
    assertGolden(
        "security/anonymous-unknown-route-under-studies",
        client.get("/studies/" + STUDY_ID + "/no-such-route/x"),
        normalizer);
    assertGolden(
        "security/anonymous-server-exception",
        client.get("/studies/" + STUDY_ID + "/analysis/NO-SUCH-ANALYSIS"),
        normalizer);
    assertGolden(
        "security/anonymous-forced-500",
        client.get("/export/studies/" + FAILING_EXPORT_STUDY_ID),
        normalizer);
  }

  @Test
  public void actuator_statusWithAndWithoutToken() {
    val managementClient = new RawHttpClient("http://localhost:" + managementPort);
    val systemToken = jwt(List.of(systemScope()), false);
    val statuses = MAPPER.createObjectNode();
    for (val path : List.of("/actuator", "/actuator/health", "/actuator/info", "/actuator/env")) {
      statuses.put("GET " + path + " noToken", managementClient.get(path).getStatus());
      statuses.put(
          "GET " + path + " systemScope",
          managementClient.withBearerToken(systemToken).get(path).getStatus());
    }
    assertMatchesGolden("security/actuator-secure", statuses);
  }

  private ObjectNode introspectionBody(List<String> scopes) {
    val body = MAPPER.createObjectNode();
    body.put("exp", System.currentTimeMillis() / 1000 + 3600);
    body.put("user_id", "c9c0d0c2-5f37-4d34-8e46-9f2d0e2b9d1e");
    val scopeArray = body.putArray("scope");
    scopes.forEach(scopeArray::add);
    return body;
  }

  /** Registers an introspection response and returns the API key (a UUID) that receives it. */
  private String apiKeyWithIntrospection(int status, ObjectNode body) {
    val apiKey = UUID.randomUUID().toString();
    INTROSPECTION_SERVER.stub(apiKey, status, body.toString());
    return apiKey;
  }

  @Test
  public void apiKey_statusPerIntrospectionResult() {
    val apiKeys = new LinkedHashMap<String, String>();
    apiKeys.put(
        "validSystemScope",
        apiKeyWithIntrospection(200, introspectionBody(List.of(systemScope()))));
    apiKeys.put(
        "validStudyScope",
        apiKeyWithIntrospection(200, introspectionBody(List.of(studyScope(STUDY_ID)))));
    apiKeys.put(
        "validOtherStudyScope",
        apiKeyWithIntrospection(200, introspectionBody(List.of(studyScope(OTHER_STUDY_ID)))));
    apiKeys.put("validNoScopes", apiKeyWithIntrospection(200, introspectionBody(List.of())));
    apiKeys.put(
        "revoked",
        apiKeyWithIntrospection(
            200, introspectionBody(List.of(systemScope())).put("revoked", true)));
    apiKeys.put(
        "validFalse",
        apiKeyWithIntrospection(
            200, introspectionBody(List.of(systemScope())).put("valid", false)));
    apiKeys.put(
        "expZero",
        apiKeyWithIntrospection(200, introspectionBody(List.of(systemScope())).put("exp", 0)));
    apiKeys.put(
        "errorField",
        apiKeyWithIntrospection(
            200, introspectionBody(List.of(systemScope())).put("error", "invalid_token")));
    apiKeys.put(
        "introspection401",
        apiKeyWithIntrospection(401, MAPPER.createObjectNode().put("error", "unauthorized")));
    apiKeys.put(
        "introspection500",
        apiKeyWithIntrospection(500, MAPPER.createObjectNode().put("error", "server")));
    apiKeys.put("unknownToIntrospection", UUID.randomUUID().toString());

    val statuses = MAPPER.createObjectNode();
    apiKeys.forEach(
        (caseName, apiKey) -> {
          val request = securedEndpoints().get("POST /submit/{studyId}").get();
          statuses.put(caseName, send(request, apiKey).getStatus());
        });
    assertMatchesGolden("security/api-key-submit", statuses);
  }

  /**
   * The introspection request carries the client credentials as basic auth and the key in the
   * configured multipart form field.
   */
  @Test
  public void apiKey_introspectionRequestShape() {
    val apiKey = apiKeyWithIntrospection(200, introspectionBody(List.of(systemScope())));
    send(securedEndpoints().get("POST /submit/{studyId}").get(), apiKey);

    val request =
        INTROSPECTION_SERVER.getRecordedRequests().stream()
            .filter(recorded -> recorded.getBody().contains(apiKey))
            .findFirst()
            .orElseThrow();
    val expectedCredentials =
        Base64.getEncoder()
            .encodeToString(
                (securityConfig.getClientId() + ":" + securityConfig.getClientSecret())
                    .getBytes(StandardCharsets.UTF_8));
    assertEquals("Basic " + expectedCredentials, request.getAuthorization());
    assertTrue(
        request.getContentType().startsWith("multipart/form-data"), request.getContentType());
    assertTrue(
        request.getBody().contains("name=\"" + securityConfig.getTokenName() + "\""),
        request.getBody());
  }

  @Test
  public void apiKey_systemScopeOnSchemaRegistration() {
    val systemApiKey = apiKeyWithIntrospection(200, introspectionBody(List.of(systemScope())));
    val studyApiKey =
        apiKeyWithIntrospection(200, introspectionBody(List.of(studyScope(STUDY_ID))));

    val statuses = MAPPER.createObjectNode();
    val request = securedEndpoints().get("POST /schemas").get();
    statuses.put("systemScope", send(request, systemApiKey).getStatus());
    statuses.put("studyScope", send(request, studyApiKey).getStatus());
    assertMatchesGolden("security/api-key-schemas", statuses);
  }
}
