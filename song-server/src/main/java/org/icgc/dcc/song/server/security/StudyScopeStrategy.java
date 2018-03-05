/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.security;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.icgc.dcc.common.core.util.Joiners.DOT;

@Slf4j
@Component
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
    return DOT.join(scopePrefix, studyId.toUpperCase(), scopeSuffix);
  }

  private String getSystemScope(){
    return DOT.join(scopePrefix,scopeSuffix);
  }

  private boolean verify(@NonNull Set<String> grantedScopes, @NonNull final String studyId) {
    val check = grantedScopes.stream().filter(s -> isGranted(s,studyId)).collect(toList());
    return !check.isEmpty();
  }

}
