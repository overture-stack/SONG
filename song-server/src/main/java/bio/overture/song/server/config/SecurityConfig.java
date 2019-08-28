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

import bio.overture.song.server.security.StudyScopeMatcher;
import bio.overture.song.server.security.SystemScopeMatcher;
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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@Component
@Validated
@Profile("secure")
@EnableWebSecurity
@EnableResourceServer
@ConfigurationProperties("auth.server.scope")
public class SecurityConfig extends ResourceServerConfigurerAdapter {

  @Autowired private SwaggerConfig swaggerConfig;

  private final ScopeConfig study = new ScopeConfig();
  private final ScopeConfig system = new ScopeConfig();

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
  public StudyScopeMatcher studyScopeMatcher() {
    return StudyScopeMatcher.builder()
        .prefix(study.getPrefix())
        .firstDelimiter(study.getFirstDelimiter())
        .secondDelimiter(study.getSecondDelimiter())
        .suffix(study.getSuffix())
        .build();
  }

  @Bean
  public SystemScopeMatcher systemScopeMatcher() {
    return SystemScopeMatcher.builder()
        .prefix(system.getPrefix())
        .suffix(system.getSuffix())
        .build();
  }

  @Getter
  @Setter
  @Validated
  public static class ScopeConfig {

    @NotNull
    @Pattern(regexp = "^\\w+")
    private String prefix;

    @Pattern(regexp = "^\\W$")
    private String firstDelimiter;

    @Pattern(regexp = "^\\W$")
    private String secondDelimiter;

    @NotNull
    @Pattern(regexp = "^\\w+")
    private String suffix;
  }

}
