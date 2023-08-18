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

import java.util.Set;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Slf4j
@Value
public class SystemSecurity {

  @NonNull private final String systemScope;
  @NonNull private final String introspectionUri;

  @Autowired
  public KeycloakAuthorizationService keycloakAuthorizationService() {
    return KeycloakAuthorizationService.builder()
        .introspectionUri(introspectionUri)
        .build();
  }

  public boolean authorize(@NonNull Authentication authentication) {
    log.debug("Checking system-level authorization");

    val authGrants = keycloakAuthorizationService()
        .fetchAuthorizationGrants(((JwtAuthenticationToken) authentication).getToken().getTokenValue());

    val grantedScopes = extractGrantedScopesFromRpt(authGrants);
    return verifyOneOfSystemScope(grantedScopes);
  }

  public boolean verifyOneOfSystemScope(@NonNull Set<String> grantedScopes) {
    return grantedScopes.stream().anyMatch(this::isGrantedForSystem);
  }

  public boolean isGrantedForSystem(@NonNull String tokenScope) {
    log.debug(
        "Checking if input scope '{}' is granted for system scope '{}'",
        tokenScope,
        getSystemScope());
    return getSystemScope().equals(tokenScope);
  }
}
