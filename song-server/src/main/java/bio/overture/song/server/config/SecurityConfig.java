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

import bio.overture.song.server.security.ApiKeyIntrospector;
import bio.overture.song.server.security.StudySecurity;
import bio.overture.song.server.security.SystemSecurity;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Getter
@Setter
@Component
@Validated
@Profile("secure")
@EnableWebSecurity
@ConfigurationProperties("auth.server")
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired private SwaggerConfig swaggerConfig;
  @Autowired private JwtDecoder jwtDecoder;

  private String introspectionUri;
  private String clientId;
  private String clientSecret;
  private String provider;
  private String tokenName;

  private final ScopeConfig scope = new ScopeConfig();

  @Bean
  public SystemSecurity systemSecurity() {
    return SystemSecurity.builder().systemScope(scope.getSystem()).provider(provider).build();
  }

  @Bean
  public AuthenticationManagerResolver<HttpServletRequest> tokenAuthenticationManagerResolver() {

    // Auth Managers for JWT and for ApiKeys. JWT uses the default auth provider,
    // but OpaqueTokens are handled by the custom ApiKeyIntrospector
    AuthenticationManager jwt = new ProviderManager(new JwtAuthenticationProvider(jwtDecoder));
    AuthenticationManager opaqueToken =
        new ProviderManager(
            new OpaqueTokenAuthenticationProvider(
                new ApiKeyIntrospector(introspectionUri, clientId, clientSecret, tokenName)));

    return (request) -> useJwt(request) ? jwt : opaqueToken;
  }

  @Bean
  public StudySecurity studySecurity() {
    return StudySecurity.builder()
        .studyPrefix(scope.getStudy().getPrefix())
        .studySuffix(scope.getStudy().getSuffix())
        .systemScope(scope.getSystem())
        .provider(provider)
        .build();
  }

  @Bean
  public OpaqueTokenIntrospector introspector() {
    return new ApiKeyIntrospector(introspectionUri, clientId, clientSecret, tokenName);
  }

  @Override
  @SneakyThrows
  public void configure(HttpSecurity http) {
    http.authorizeRequests()
        .antMatchers("/isAlive")
        .permitAll()
        .antMatchers("/studies/**")
        .permitAll()
        .antMatchers("/upload/**")
        .permitAll()
        .antMatchers("/entities/**")
        .permitAll()
        .antMatchers("/export/**")
        .permitAll()
        .antMatchers("/schemas/**")
        .permitAll()
        .antMatchers(swaggerConfig.getAlternateSwaggerUrl())
        .permitAll()
        .antMatchers("/swagger**", "/swagger-resources/**", "/v2/api**", "/webjars/**")
        .permitAll()
        .and()
        .authorizeRequests()
        .anyRequest()
        .authenticated();

    http.oauth2ResourceServer(
        oauth2 -> oauth2.authenticationManagerResolver(this.tokenAuthenticationManagerResolver()));
  }

  @Getter
  @Setter
  public static class ScopeConfig {

    @NotNull private String system;
    private final StudyScopeConfig study = new StudyScopeConfig();

    @Getter
    @Setter
    public static class StudyScopeConfig {

      @NotNull
      @Pattern(regexp = "^\\w+\\W$")
      private String prefix;

      @NotNull
      @Pattern(regexp = "^\\W\\w+$")
      private String suffix;
    }
  }

  private boolean useJwt(HttpServletRequest request) {
    val authorizationHeaderValue = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer")) {
      String token = authorizationHeaderValue.substring(7);
      try {
        UUID.fromString(token);
        // able to parse as UUID, so this token matches our ApiKey format
        return false;
      } catch (IllegalArgumentException e) {
        // unable to parse as UUID, use our JWT resolvers
        return true;
      }
    }
    return true;
  }
}
