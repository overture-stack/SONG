package bio.overture.song.server.security;

import static bio.overture.song.core.exceptions.ServerErrors.FORBIDDEN_TOKEN;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.utils.Joiners.WHITESPACE;
import static bio.overture.song.server.oauth.ExpiringOauth2Authentication.from;
import static bio.overture.song.server.utils.Scopes.extractExpiry;
import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Slf4j
public class JWTTokenConverter extends JwtAccessTokenConverter {

  private static final String CONTEXT = "context";
  private static final String SCOPE = "scope";
  private static final String CONTEXT_SCOPE_FIELD_NAME = CONTEXT + "." + SCOPE;

  @SneakyThrows
  public JWTTokenConverter(@NonNull String publicKey) {
    super();
    this.setVerifierKey(publicKey);
    this.afterPropertiesSet();
  }

  @Override
  public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
    // TODO: rtisma --- this is a hack since EGO does not implement jwts correctly
    val mutatedMap = mutateMap(map);

    final long secondsUntilExpiry = calcSecondsUntilExpiry(map);
    val authentication = from(super.extractAuthentication(mutatedMap), secondsUntilExpiry);

    // TODO: rtisma --- this is also a hack. the resourceIds maps to the "aud" field. This should be
    // empty inorder for the OAuth2AuthenticationManager to process properly
    authentication.getOAuth2Request().getResourceIds().clear();

    return authentication;
  }

  private static long calcSecondsUntilExpiry(Map<String, ?> map) {
    final long expirationTimestamp = extractExpiry(map);
    final long diff = expirationTimestamp - MILLISECONDS.toSeconds(currentTimeMillis());
    return max(diff, 0L);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, ?> parseContextMap(Map<String, ?> map) {
    if (map.containsKey(CONTEXT)) {
      return (Map<String, ?>) map.get(CONTEXT);
    }
    val timestamp = currentTimeMillis();
    log.error("[@{}] JWTToken is missing '{}' field", timestamp, CONTEXT);
    throw buildServerException(
        JWTTokenConverter.class, FORBIDDEN_TOKEN, "[@%s] Token is not authorized", timestamp);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, ?> mutateMap(Map<String, ?> map) {
    val context = parseContextMap(map);
    val mutatedMap = new HashMap<String, Object>(map);
    if (context.containsKey(SCOPE)) {
      val egoScopes = (List<String>) context.get(SCOPE);
      mutatedMap.put(SCOPE, WHITESPACE.join(egoScopes));
    } else {
      val timestamp = currentTimeMillis();
      log.error("[@{}] JWTToken is missing '{}' field", timestamp, CONTEXT_SCOPE_FIELD_NAME);
      throw buildServerException(
          JWTTokenConverter.class, FORBIDDEN_TOKEN, "[@%s] Token is not authorized", timestamp);
    }
    return mutatedMap;
  }
}
