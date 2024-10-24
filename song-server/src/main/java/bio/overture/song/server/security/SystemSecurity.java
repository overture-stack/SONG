/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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
package bio.overture.song.server.security;

import static bio.overture.song.server.utils.Scopes.extractGrantedScopes;
import static bio.overture.song.server.utils.Scopes.extractGrantedScopesFromRpt;

import bio.overture.song.server.service.auth.KeycloakAuthorizationService;
import java.util.Set;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Slf4j
@Builder
public class SystemSecurity {

  @NonNull private final String systemScope;
  private final String provider;

  @Autowired private KeycloakAuthorizationService keycloakAuthorizationService;

  public boolean authorize(@NonNull Authentication authentication) {
    log.debug("Checking system-level authorization");

    Set<String> grantedScopes;

    if ("keycloak".equalsIgnoreCase(provider) && authentication instanceof JwtAuthenticationToken) {

      val authGrants =
          keycloakAuthorizationService.fetchAuthorizationGrants(
              ((JwtAuthenticationToken) authentication).getToken().getTokenValue());

      grantedScopes = extractGrantedScopesFromRpt(authGrants);
    } else {
      // extract scopes from authentication object
      grantedScopes = extractGrantedScopes(authentication);
    }

    return verifyOneOfSystemScope(grantedScopes);
  }

  public boolean verifyOneOfSystemScope(@NonNull Set<String> grantedScopes) {
    return grantedScopes.stream().anyMatch(this::isGrantedForSystem);
  }

  public boolean isGrantedForSystem(@NonNull String tokenScope) {
    log.debug(
        "Checking if input scope '{}' is granted for system scope '{}'", tokenScope, systemScope);
    return systemScope.equals(tokenScope);
  }
}
