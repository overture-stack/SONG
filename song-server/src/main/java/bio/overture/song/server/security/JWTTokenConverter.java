package bio.overture.song.server.security;

import static bio.overture.song.core.exceptions.ServerErrors.UNAUTHORIZED_TOKEN;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.utils.Joiners.WHITESPACE;
import static bio.overture.song.server.oauth.ExpiringOauth2Authentication.from;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.JWTApplication;
import bio.overture.song.server.model.JWTUser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import bio.overture.song.server.oauth.ExpiringOauth2Authentication;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Slf4j
public class JWTTokenConverter extends JwtAccessTokenConverter {

  private static final String CONTEXT = "context";
  private static final String USER = "user";
  private static final String APPLICATION = "application";
  private static final String SCOPE = "scope";
  private static final String CONTEXT_USER_FIELD_NAME = CONTEXT + "." + USER;
  private static final String CONTEXT_APPLICATION_FIELD_NAME = CONTEXT + "." + APPLICATION;
  private static final String CONTEXT_SCOPE_FIELD_NAME = CONTEXT + "." + SCOPE;

  @SneakyThrows
  public JWTTokenConverter(@NonNull String publicKey) {
    super();
    this.setVerifierKey(publicKey);
    this.afterPropertiesSet();
  }

  @Override
  public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
    // Extract principle details
    val principleDetails = extractPrincipleDetails(map);

    // TODO: rtisma --- this is a hack since EGO does not implement jwts correctly
    val mutatedMap = mutateMap(map);

    val expirationTimestamp = parseExpirationTimestamp(map);
    val secondsUntilExpiry = calcSecondsUntilExpiry(expirationTimestamp);
    ExpiringOauth2Authentication authentication = from(super.extractAuthentication(mutatedMap), secondsUntilExpiry);

    // TODO: rtisma --- this is also a hack. the resourceIds maps to the "aud" field. This should be
    // empty inorder for the OAuth2AuthenticationManager to process properly
    authentication.getOAuth2Request().getResourceIds().clear();

    authentication.setDetails(principleDetails);
    return authentication;
  }

  private static long calcSecondsUntilExpiry(Long expirationTimestamp) {
    val diff = expirationTimestamp - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    return diff > 0 ? diff : 0;
  }

  private static Long parseExpirationTimestamp(Map<String, ?> map){
    val exp = map.get("exp");
    return (exp instanceof Long) ? (Long) exp : 0;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, ?> parseContextMap(Map<String, ?> map) {
    if (map.containsKey(CONTEXT)) {
      return (Map<String, ?>) map.get(CONTEXT);
    }
    val timestamp = System.currentTimeMillis();
    log.error("[@{}] JWTToken is missing '{}' field", CONTEXT, timestamp);
    throw buildServerException(
        JWTTokenConverter.class, UNAUTHORIZED_TOKEN, "[@%s] Token is not authorized", timestamp);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, ?> mutateMap(Map<String, ?> map) {
    val context = parseContextMap(map);
    val mutatedMap = new HashMap<String, Object>(map);
    if (context.containsKey(SCOPE)) {
      val egoScopes = (List<String>) context.get(SCOPE);
      mutatedMap.put(SCOPE, WHITESPACE.join(egoScopes));
    } else {
      val timestamp = System.currentTimeMillis();
      log.error("[@{}] JWTToken is missing '{}' field", CONTEXT_SCOPE_FIELD_NAME, timestamp);
      throw buildServerException(
          JWTTokenConverter.class, UNAUTHORIZED_TOKEN, "[@%s] Token is not authorized", timestamp);
    }
    return mutatedMap;
  }

  private static Object extractPrincipleDetails(Map<String, ?> map) {
    return parseContextUser(map)
        .or(() -> parseContextApplication(map))
        .orElseThrow(
            () -> {
              val timestamp = System.currentTimeMillis();
              log.error(
                  "[@{}] JWT Token must have at least one field of: {}, {}",
                  CONTEXT_USER_FIELD_NAME,
                  CONTEXT_APPLICATION_FIELD_NAME,
                  timestamp);
              throw buildServerException(
                  JWTTokenConverter.class,
                  UNAUTHORIZED_TOKEN,
                  "[@%s] Token is not authorized",
                  timestamp);
            });
  }

  private static Optional<Object> parseContextUser(Map<String, ?> map) {
    return parseContextPrinciple(map, JWTUser.class, USER);
  }

  private static Optional<Object> parseContextApplication(Map<String, ?> map) {
    return parseContextPrinciple(map, JWTApplication.class, APPLICATION);
  }

  private static <P> Optional<Object> parseContextPrinciple(
      Map<String, ?> map, Class<P> principleClass, String principleKey) {
    val context = parseContextMap(map);
    if (context.containsKey(principleKey)) {
      val principle = context.get(principleKey);
      return Optional.of(JsonUtils.mapper().convertValue(principle, principleClass));
    }
    return Optional.empty();
  }
}
