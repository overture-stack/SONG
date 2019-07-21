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
package bio.overture.song.server.security;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static bio.overture.song.server.utils.Scopes.extractGrantedScopes;

@Slf4j
@Component
@Profile("secure")
public class SystemSecurity {

  private final ScopeValidator scopeValidator;

  @Autowired
  public SystemSecurity(@NonNull ScopeValidator scopeValidator) {
    this.scopeValidator = scopeValidator;
  }

  public boolean authorize(@NonNull Authentication authentication) {
    log.info("Checking system-level authorization");
    val grantedScopes = extractGrantedScopes(authentication);
    return scopeValidator.verifyOneOfSystemScope(grantedScopes);
  }

}
