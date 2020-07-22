package bio.overture.song.server.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Strings;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

@RequiredArgsConstructor
public class CustomResourceServerTokenServices implements ResourceServerTokenServices {

  private final RemoteTokenServices apiKeyTokenServices;
  private final TokenStore jwtTokenStore;
  private final RetryTemplate retryTemplate;

  @Override
  public OAuth2Authentication loadAuthentication(String accessToken)
      throws AuthenticationException, InvalidTokenException {
    if (isApiKey(accessToken)) {
      return retryTemplate.execute(x -> apiKeyTokenServices.loadAuthentication(accessToken));
    }
    // If not apiKey, then assume JWT
    return jwtTokenStore.readAuthentication(accessToken);
  }

  @Override
  public OAuth2AccessToken readAccessToken(String accessToken) {
    if (isApiKey(accessToken)) {
      return retryTemplate.execute(x -> apiKeyTokenServices.readAccessToken(accessToken));
    }
    return jwtTokenStore.readAccessToken(accessToken);
  }

  private static boolean isApiKey(String value) {
    if (Strings.isNullOrEmpty(value)) {
      return false;
    }
    try {
      UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
}
