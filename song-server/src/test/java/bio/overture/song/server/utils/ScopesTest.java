package bio.overture.song.server.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.overture.song.server.security.KeycloakPermission;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;

/**
 * Tests scope extraction from each authentication type.
 *
 * <p>JWTs are signed and then decoded with the same {@link NimbusJwtDecoder} the server uses, so
 * the claim objects have the runtime types the server sees. Those types change between Nimbus
 * versions, and a mismatch would make every JWT appear to have no scopes.
 */
public class ScopesTest {

  private static KeyPair keyPair;
  private static NimbusJwtDecoder decoder;

  @BeforeAll
  @SneakyThrows
  public static void createKeys() {
    val keyGenerator = KeyPairGenerator.getInstance("RSA");
    keyGenerator.initialize(2048);
    keyPair = keyGenerator.genKeyPair();
    decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
  }

  @SneakyThrows
  private static JwtAuthenticationToken decodedJwt(Map<String, Object> extraClaims) {
    val claims =
        new JWTClaimsSet.Builder()
            .subject("subject")
            .issueTime(new Date())
            .expirationTime(Date.from(Instant.now().plusSeconds(3600)));
    extraClaims.forEach(claims::claim);
    val signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims.build());
    signedJwt.sign(new RSASSASigner(keyPair.getPrivate()));
    return new JwtAuthenticationToken(decoder.decode(signedJwt.serialize()));
  }

  @Test
  public void jwt_contextScopes_extracted() {
    val authentication =
        decodedJwt(Map.of("context", Map.of("scope", List.of("song.WRITE", "STUDY.X.WRITE"))));
    assertEquals(
        Set.of("song.WRITE", "STUDY.X.WRITE"), Scopes.extractGrantedScopes(authentication));
  }

  @Test
  public void jwt_contextWithOtherClaims_onlyScopesExtracted() {
    val context = Map.of("scope", List.of("song.WRITE"), "user", Map.of("name", "someone"));
    assertEquals(
        Set.of("song.WRITE"), Scopes.extractGrantedScopes(decodedJwt(Map.of("context", context))));
  }

  @Test
  public void jwt_nonStringScopes_ignored() {
    val authentication = decodedJwt(Map.of("context", Map.of("scope", List.of("song.WRITE", 5))));
    assertEquals(Set.of("song.WRITE"), Scopes.extractGrantedScopes(authentication));
  }

  @Test
  public void jwt_withoutContext_noScopes() {
    assertTrue(Scopes.extractGrantedScopes(decodedJwt(Map.of("scope", "song.WRITE"))).isEmpty());
  }

  @Test
  public void jwt_scopeNotAnArray_noScopes() {
    val authentication = decodedJwt(Map.of("context", Map.of("scope", "song.WRITE")));
    assertTrue(Scopes.extractGrantedScopes(authentication).isEmpty());
  }

  @Test
  public void apiKey_principalScopes_extracted() {
    val principal =
        new OAuth2IntrospectionAuthenticatedPrincipal(
            Map.of("scope", List.of("song.WRITE", "STUDY.X.WRITE")), List.of());
    val token =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "token",
            Instant.now(),
            Instant.now().plusSeconds(60));
    val authentication = new BearerTokenAuthentication(principal, token, List.of());
    assertEquals(
        Set.of("song.WRITE", "STUDY.X.WRITE"), Scopes.extractGrantedScopes(authentication));
  }

  @Test
  public void otherAuthenticationType_noScopes() {
    val principal =
        new DefaultOAuth2AuthenticatedPrincipal(Map.of("scope", List.of("song.WRITE")), List.of());
    val authentication = new TestingAuthenticationToken(principal, null);
    assertTrue(Scopes.extractGrantedScopes(authentication).isEmpty());
  }

  @Test
  public void rpt_permissionsCombineResourceNameAndScope() {
    val permissions =
        List.of(
            new KeycloakPermission("r1", "STUDY.X", List.of("WRITE", "READ")),
            new KeycloakPermission("r2", "song", List.of("WRITE")),
            new KeycloakPermission("r3", "ignored", null));
    assertEquals(
        Set.of("STUDY.X.WRITE", "STUDY.X.READ", "song.WRITE"),
        Scopes.extractGrantedScopesFromRpt(permissions));
  }

  @Test
  public void extractExpiry_integerLongAndOther() {
    assertEquals(5L, Scopes.extractExpiry(Map.of("exp", 5)));
    assertEquals(6L, Scopes.extractExpiry(Map.of("exp", 6L)));
    assertEquals(0L, Scopes.extractExpiry(Map.of("exp", "7")));
    assertEquals(0L, Scopes.extractExpiry(Map.of()));
  }
}
