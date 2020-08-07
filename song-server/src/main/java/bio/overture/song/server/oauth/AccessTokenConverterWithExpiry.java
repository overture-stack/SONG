package bio.overture.song.server.oauth;

import static bio.overture.song.server.oauth.ExpiringOauth2Authentication.from;
import static bio.overture.song.server.utils.Scopes.extractExpiry;

import java.util.Map;

import bio.overture.song.server.utils.Scopes;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;

/**
 * * RemoteTokenServices uses a postForMap call to convert the Oauth2 JSON response that we get from
 * Ego into a Java Map from string to an unknown object type.
 *
 * <p>The default converter just extracts the scope field; we also want to extract the "exp" field,
 * which holds the time to expiry for our token in seconds.
 */
public class AccessTokenConverterWithExpiry extends DefaultAccessTokenConverter {

  @Override
  public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
    long expiryInSeconds = extractExpiry(map);
    return from(super.extractAuthentication(map), expiryInSeconds);
  }

}
