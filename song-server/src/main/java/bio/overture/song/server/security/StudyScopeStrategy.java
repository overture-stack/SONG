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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.common.core.util.Joiners.DOT;

@Slf4j
public class StudyScopeStrategy {

  @Value("${auth.server.prefix}")
  protected String scopePrefix;

  @Value("${auth.server.suffix}")
  protected String scopeSuffix;


  public boolean authorize(@NonNull Authentication authentication, @NonNull final String studyId) {
    log.info("Checking authorization with study id {}", studyId);

    // if not OAuth2, then no scopes available at all
    Set<String> grantedScopes = Collections.emptySet();
    if (authentication instanceof OAuth2Authentication) {
      OAuth2Authentication o2auth = (OAuth2Authentication) authentication;
      grantedScopes = getScopes(o2auth);
    }

    return verify(grantedScopes, studyId);
  }

  private Set<String> getScopes(@NonNull OAuth2Authentication o2auth) {
    return o2auth.getOAuth2Request().getScope();
  }

  private boolean isGranted(String tokenScope, String studyId) {
    log.info("Checking token's scope '{}', server's scopePrefix='{}', studyId '{}', scopeSuffix='{}'",
        tokenScope, scopePrefix, studyId, scopeSuffix);
    return getSystemScope().equals(tokenScope) || getEndUserScope(studyId).equals(tokenScope); //short-circuit
  }

  private String getEndUserScope(String studyId){
    return DOT.join(scopePrefix, studyId, scopeSuffix);
  }

  private String getSystemScope(){
    return DOT.join(scopePrefix,scopeSuffix);
  }

  public boolean verify(@NonNull Set<String> grantedScopes, @NonNull final String studyId) {
    val check = grantedScopes.stream().filter(s -> isGranted(s,studyId)).collect(toList());
    return !check.isEmpty();
  }

}
