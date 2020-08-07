package bio.overture.song.server.utils;

import static lombok.AccessLevel.PRIVATE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

@NoArgsConstructor(access = PRIVATE)
public class Scopes {

  private static final String EXP = "exp";

  public static Set<String> extractGrantedScopes(Authentication authentication) {
    // if not OAuth2, then no scopes available at all
    Set<String> grantedScopes = Collections.emptySet();
    if (authentication instanceof OAuth2Authentication) {
      OAuth2Authentication o2auth = (OAuth2Authentication) authentication;
      grantedScopes = getScopes(o2auth);
    }
    return grantedScopes;
  }

  public static long extractExpiry(Map<String, ?> map) {
    Object exp = map.get(EXP);
    if (exp instanceof Integer) {
      return (Integer) exp;
    } else if (exp instanceof Long) {
      return (Long) exp;
    }
    return 0L;
  }

  private static Set<String> getScopes(OAuth2Authentication o2auth) {
    return o2auth.getOAuth2Request().getScope();
  }
}
