package bio.overture.song.server.security;

import static bio.overture.song.server.utils.contract.GoldenFiles.assertMatchesGolden;
import static bio.overture.song.server.utils.contract.RawHttpClient.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.contract.RawResponse;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

/**
 * Locks authorization with the {@code keycloak} provider, the production default, with golden files
 * under {@code golden/security}.
 *
 * <p>With this provider the JWT's own scopes are ignored. For each secured request the server
 * exchanges the caller's JWT at Keycloak's token endpoint for its UMA permissions, and builds each
 * granted scope as {@code <rsname>.<scope>}. The token endpoint is stubbed per JWT.
 */
@TestPropertySource(properties = "auth.server.provider=keycloak")
public class KeycloakSecurityContractTest extends AbstractSecurityContractTest {

  private static final String STUDY_ID = "KEYCLOAK-STUDY";
  private static final String OTHER_STUDY_ID = "KEYCLOAK-OTHER";

  @Autowired private StudyService studyService;

  @BeforeAll
  public void setUpData() {
    studyService.saveStudy(Study.builder().studyId(STUDY_ID).name(STUDY_ID).build());
  }

  /**
   * Returns a JWT for which the stubbed Keycloak token endpoint answers {@code status} and {@code
   * responseBody}. The JWT carries only a unique placeholder scope, which the keycloak provider
   * ignores; it keeps JWTs issued in the same second distinct, so stubs never overlap.
   */
  private String jwtWithKeycloakResponse(int status, String responseBody) {
    val token = jwt(List.of("unique." + UUID.randomUUID()), false);
    AUTH_SERVER.stubFor(
        post(urlEqualTo(KEYCLOAK_TOKEN_PATH))
            .withHeader("Authorization", equalTo("Bearer " + token))
            .willReturn(
                aResponse()
                    .withStatus(status)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody)));
    return token;
  }

  /** A UMA permission whose granted scopes are {@code <rsname>.<scope>} for each scope. */
  private static void addPermission(ArrayNode permissions, String rsname, String... scopes) {
    val permission = permissions.addObject();
    permission.put("rsid", "resource-" + rsname);
    permission.put("rsname", rsname);
    val scopeArray = permission.putArray("scopes");
    for (val scope : scopes) {
      scopeArray.add(scope);
    }
  }

  /** Splits a configured scope such as {@code STUDY.X.WRITE} into its rsname and last part. */
  private static String[] rsnameAndScope(String fullScope) {
    val lastDot = fullScope.lastIndexOf('.');
    return new String[] {fullScope.substring(0, lastDot), fullScope.substring(lastDot + 1)};
  }

  private String jwtGranting(String fullScope) {
    val parts = rsnameAndScope(fullScope);
    val permissions = MAPPER.createArrayNode();
    addPermission(permissions, parts[0], parts[1]);
    return jwtWithKeycloakResponse(200, permissions.toString());
  }

  private RawResponse submit(String token) {
    return client
        .withBearerToken(token)
        .send(
            "POST",
            "/submit/" + STUDY_ID + "?allowDuplicates=true",
            APPLICATION_JSON,
            readVariantCallPayload(STUDY_ID).toString());
  }

  private RawResponse registerSchema(String token) {
    val request = MAPPER.createObjectNode().put("name", "keycloakType");
    request
        .putObject("schema")
        .put("type", "object")
        .putObject("properties")
        .putObject("note")
        .put("type", "string");
    return client.withBearerToken(token).post("/schemas", request.toString());
  }

  @Test
  public void submit_statusPerKeycloakPermission() {
    val tokens = new LinkedHashMap<String, String>();
    tokens.put("noToken", null);
    tokens.put("studyPermission", jwtGranting(studyScope(STUDY_ID)));
    tokens.put("otherStudyPermission", jwtGranting(studyScope(OTHER_STUDY_ID)));
    tokens.put("systemPermission", jwtGranting(systemScope()));
    tokens.put("noPermissions", jwtWithKeycloakResponse(200, "[]"));
    tokens.put(
        "permissionWithoutScopes",
        jwtWithKeycloakResponse(200, "[{\"rsid\": \"r\", \"rsname\": \"" + STUDY_ID + "\"}]"));
    tokens.put("keycloak401", jwtWithKeycloakResponse(401, "{\"error\": \"unauthorized\"}"));
    tokens.put("keycloak403", jwtWithKeycloakResponse(403, "{\"error\": \"access_denied\"}"));
    tokens.put("keycloak500", jwtWithKeycloakResponse(500, "{\"error\": \"server\"}"));

    val statuses = MAPPER.createObjectNode();
    tokens.forEach((caseName, token) -> statuses.put(caseName, submit(token).getStatus()));
    assertMatchesGolden("security/keycloak-submit", statuses);
  }

  @Test
  public void schemaRegistration_statusPerKeycloakPermission() {
    val statuses = MAPPER.createObjectNode();
    statuses.put("systemPermission", registerSchema(jwtGranting(systemScope())).getStatus());
    statuses.put("studyPermission", registerSchema(jwtGranting(studyScope(STUDY_ID))).getStatus());
    assertMatchesGolden("security/keycloak-schemas", statuses);
  }

  @Test
  public void umaRequest_carriesConfiguredParameters() {
    val token = jwtGranting(systemScope());
    submit(token);
    AUTH_SERVER.verify(
        postRequestedFor(urlEqualTo(KEYCLOAK_TOKEN_PATH))
            .withHeader("Authorization", equalTo("Bearer " + token))
            .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
            .withRequestBody(
                containing("grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Auma-ticket"))
            .withRequestBody(containing("response_mode=permissions"))
            .withRequestBody(containing("audience=")));
  }
}
