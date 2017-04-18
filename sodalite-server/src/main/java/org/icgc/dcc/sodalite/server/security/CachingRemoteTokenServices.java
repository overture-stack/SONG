package org.icgc.dcc.sodalite.server.security;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

public class CachingRemoteTokenServices extends RemoteTokenServices {

  @Override
  @Cacheable("tokens")
  public OAuth2Authentication loadAuthentication(String accessToken)
      throws AuthenticationException, InvalidTokenException {
    return super.loadAuthentication(accessToken);
  }

}
