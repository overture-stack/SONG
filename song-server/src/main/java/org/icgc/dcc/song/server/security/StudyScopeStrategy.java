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

import java.util.Collections;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Slf4j
public class StudyScopeStrategy {

  private static final String SCOPE_STRATEGY = "song.%s.%s";

  @Value("${auth.server.uploadScope}")
  protected String uploadScope;

  public boolean authorize(@NonNull Authentication authentication, @NonNull final String studyId) {
    log.info("Checking authorization with study analysisId {}", studyId);

    // if not OAuth2, then no scopes available at all
    Set<String> grantedScopes = Collections.emptySet();
    if (authentication instanceof OAuth2Authentication) {
      OAuth2Authentication o2auth = (OAuth2Authentication) authentication;
      grantedScopes = getScopes(o2auth);
    }

    return verify(grantedScopes, studyId);
  }

  protected void setAuthorizeScope(String scopeStr) {
    uploadScope = scopeStr;
  }

  protected String getAuthorizeScope() {
    return uploadScope;
  }

  private Set<String> getScopes(@NonNull OAuth2Authentication o2auth) {
    return o2auth.getOAuth2Request().getScope();
  }

  private boolean verify(@NonNull Set<String> grantedScopes, @NonNull final String studyId) {
    val strategy = format(SCOPE_STRATEGY, studyId.toUpperCase(), uploadScope);
    val check = grantedScopes.stream().filter(s -> s.equals(strategy)).collect(toList());
    return !check.isEmpty();
  }

}
