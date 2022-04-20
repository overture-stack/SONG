package bio.overture.song.server.utils;

import static lombok.AccessLevel.PRIVATE;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class Scopes {

  private static final String EXP = "exp";

  public static Set<String> extractGrantedScopes(Authentication authentication) {
    // if not OAuth2, then no scopes available at all
    Set<String> grantedScopes = Collections.emptySet();
    if (authentication instanceof JwtAuthenticationToken) {
      grantedScopes = getScopes((JwtAuthenticationToken) authentication);
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

  private static Set<String> getScopes(JwtAuthenticationToken jwt) {
    Set<String> output = new HashSet();
    try {
      val context = jwt.getToken().getClaim("context");
      if (context instanceof JSONObject) {
        val scopes = ((JSONObject) context).get("scope");
        if (scopes instanceof JSONArray) {
          val scopeArray = (JSONArray) scopes;
          scopeArray.stream()
              .filter(value -> value instanceof String)
              .forEach(value -> output.add((String) value));
        }
      }
    }
    //    catch () {
    //      log.debug("Received JWT not structured as expected. No scopes found.");
    //    }
    catch (ClassCastException e) {
      log.debug("Received JWT not structured as expected. No scopes found.");
    }
    return output;
  }
}
