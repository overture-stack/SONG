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

import bio.overture.song.server.security.StudyScopeStrategy;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.stereotype.Component;

@Profile("secure")
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

  @Value("${auth.server.prefix}")
  private String scopePrefix;

  @Value("${auth.server.suffix}")
  private String scopeSuffix;

    /**
     * Refer:
   * http://stackoverflow.com/questions/29328124/no-bean-resolver-registered-in-the-context-to-resolve-access-to-bean
   *
   * The following lines are a workaround suggested here:
   * https://github.com/spring-projects/spring-security-oauth/issues/730
   *
   * Apparently a bug in Spring's OAuth2 stuff - BeanResolver is not being set in the Application Context, so attempting
   * to evaluate a bean lookup @beanName blows up
   */
  @Autowired
  private ApplicationContext context;

  @Override
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    OAuth2MethodSecurityExpressionHandler handler = new OAuth2MethodSecurityExpressionHandler();
    handler.setApplicationContext(context);
    return handler;
  }

  @Bean
  public StudyScopeStrategy studySecurity(){
    return new StudyScopeStrategy(scopePrefix, scopeSuffix);
  }

}
