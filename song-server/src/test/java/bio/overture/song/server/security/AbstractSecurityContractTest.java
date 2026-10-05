package bio.overture.song.server.security;

import static bio.overture.song.core.utils.JsonUtils.toJson;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;

import bio.overture.song.server.config.SecurityConfig;
import bio.overture.song.server.contract.AbstractContractTest;
import bio.overture.song.server.utils.contract.IntrospectionServer;
import bio.overture.song.server.utils.jwt.JWTGenerator;
import bio.overture.song.server.utils.jwt.JwtContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base for security tests that call a real server running the {@code secure} profile.
 *
 * <p>A WireMock server stands in for the JWKS endpoint that verifies test JWTs and for the Keycloak
 * UMA token endpoint; an {@link IntrospectionServer} stands in for the API key introspection
 * endpoint. Both start before the Spring context so the auth properties can point at their ports.
 */
@ActiveProfiles({"test", "secure", "jwt"})
public abstract class AbstractSecurityContractTest extends AbstractContractTest {

  protected static final String JWK_SET_PATH = "/realms/myrealm/protocol/openid-connect/certs";
  protected static final String KEYCLOAK_TOKEN_PATH =
      "/realms/myrealm/protocol/openid-connect/token";

  protected static final WireMockServer AUTH_SERVER = new WireMockServer(options().dynamicPort());

  protected static final IntrospectionServer INTROSPECTION_SERVER = new IntrospectionServer();

  @DynamicPropertySource
  static void registerAuthServerUrls(DynamicPropertyRegistry registry) {
    AUTH_SERVER.start();
    INTROSPECTION_SERVER.start();
    val authServerUrl = format("http://localhost:%d", AUTH_SERVER.port());
    registry.add(
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> authServerUrl + JWK_SET_PATH);
    registry.add("auth.server.introspectionUri", INTROSPECTION_SERVER::url);
    registry.add("auth.server.keycloak.host", () -> authServerUrl);
  }

  @AfterAll
  public static void stopAuthServer() {
    AUTH_SERVER.stop();
    INTROSPECTION_SERVER.stop();
  }

  @Autowired protected JWTGenerator jwtGenerator;
  @Autowired protected SecurityConfig securityConfig;
  @Autowired private KeyPair keyPair;

  @BeforeAll
  public void stubJwkSet() {
    val jwk =
        new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .build();
    AUTH_SERVER.stubFor(
        get(urlEqualTo(JWK_SET_PATH))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(toJson(new JWKSet(jwk).toJSONObject()))));
  }

  protected String systemScope() {
    return securityConfig.getScope().getSystem();
  }

  protected String studyScope(String studyId) {
    val studyScopeConfig = securityConfig.getScope().getStudy();
    return studyScopeConfig.getPrefix() + studyId + studyScopeConfig.getSuffix();
  }

  /** A signed JWT whose {@code context.scope} claim holds {@code scopes}. */
  protected String jwt(Collection<String> scopes, boolean expired) {
    return jwtGenerator.generateJwtWithContext(JwtContext.buildJwtContext(scopes), expired);
  }
}
