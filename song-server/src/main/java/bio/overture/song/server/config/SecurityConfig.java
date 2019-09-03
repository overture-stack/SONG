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

import bio.overture.song.server.security.StudySecurity;
import bio.overture.song.server.security.SystemSecurity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@Profile("secure")
@EnableWebSecurity
@EnableResourceServer
@ConfigurationProperties("auth.server")
public class SecurityConfig extends ResourceServerConfigurerAdapter {

  @Autowired private SwaggerConfig swaggerConfig;

  private final ScopeConfig scope = new ScopeConfig();

  @Override
  @SneakyThrows
  public void configure(HttpSecurity http) {
    http.authorizeRequests()
        .antMatchers("/health")
        .permitAll()
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
  }

  @Bean
  public SystemSecurity systemSecurity() {
    return new SystemSecurity(scope.getSystem());
  }

  @Bean
  public StudySecurity studySecurity(@Autowired SystemSecurity systemSecurity) {
    return StudySecurity.builder()
        .studyPrefix(scope.getStudy().getPrefix())
        .studySuffix(scope.getStudy().getSuffix())
        .systemScope(scope.getSystem())
        .build();
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
}
