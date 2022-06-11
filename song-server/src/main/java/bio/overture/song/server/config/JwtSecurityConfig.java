/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package bio.overture.song.server.config;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
@Setter
@Profile("jwt")
@Order(Ordered.LOWEST_PRECEDENCE + 10)
public class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired JwtDecoder jwtDecoder;
  @Autowired OpaqueTokenIntrospector opaqueTokenIntrospector;

  @Bean
  private AuthenticationManagerResolver<HttpServletRequest> tokenAuthenticationManagerResolver() {

    AuthenticationManager jwt = new ProviderManager(new JwtAuthenticationProvider(jwtDecoder));
    AuthenticationManager opaqueToken =
        new ProviderManager(new OpaqueTokenAuthenticationProvider(opaqueTokenIntrospector));

    return (request) -> useJwt(request) ? jwt : opaqueToken;
  }

  @Override
  @SneakyThrows
  public void configure(HttpSecurity http) {
    http.oauth2ResourceServer(
        oauth2 -> oauth2.authenticationManagerResolver(this.tokenAuthenticationManagerResolver()));
  }

  private boolean useJwt(HttpServletRequest request) {
    val authorizationHeaderValue = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer")) {
      String token = authorizationHeaderValue.substring(7, authorizationHeaderValue.length());
      try {
        UUID.fromString(token);
        // able to parse as UUID, so this token matches our EgoApiKey format
        return false;
      } catch (IllegalArgumentException e) {
        // unable to parse as UUID, use our JWT resolvers
        return true;
      }
    }
    return true;
  }
}
