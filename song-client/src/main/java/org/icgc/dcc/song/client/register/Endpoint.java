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
package org.icgc.dcc.song.client.register;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import static java.lang.String.format;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class Endpoint {

  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  @NonNull
  private String serverUrl;

  public String upload(String studyId, boolean isAsyncValidation) {
    if (isAsyncValidation){
      return format("%s/upload/%s/async", serverUrl, studyId);
    } else {
      return format("%s/upload/%s", serverUrl, studyId);
    }
  }

  public String saveById(String studyId, String uploadId, boolean ignoreAnalysisIdCollisions) {
    return format("%s/upload/%s/save/%s?ignoreAnalysisIdCollisions=%s", serverUrl, studyId, uploadId, ignoreAnalysisIdCollisions);
  }

  public String status(String studyId, String uploadId) {
    return format("%s/upload/%s/status/%s", serverUrl, studyId, uploadId);
  }

  public String getAnalysisFiles(String studyId, String analysisId) {
    return format("%s/studies/%s/analysis/%s/files", serverUrl, studyId, analysisId);
  }

  public String getAnalysis(String studyId, String analysisId) {
    return format("%s/studies/%s/analysis/%s", serverUrl, studyId, analysisId);
  }

  public String isAlive() {
    return format("%s/isAlive", serverUrl);
  }

  public String publish(String studyId, String analysisId) {
    return format("%s/studies/%s/analysis/publish/%s", serverUrl, studyId, analysisId);
  }

  public String suppress(String studyId, String analysisId) {
    return format("%s/studies/%s/analysis/suppress/%s", serverUrl, studyId, analysisId);
  }

  public String idSearch(@NonNull String studyId,
      String sampleId,
      String specimenId,
      String donorId,
      String fileId){
    val list = Lists.<String>newArrayList();
    if (!isNull(sampleId)){
      list.add("sampleId="+sampleId);
    }
    if (!isNull(specimenId)){
      list.add("specimenId="+specimenId);
    }
    if (!isNull(donorId)){
      list.add("donorId="+donorId);
    }
    if (!isNull(fileId)){
      list.add("fileId="+fileId);
    }
    val params = AMPERSAND_JOINER.join(list);
    return format("%s/studies/%s/analysis/search/id?%s", serverUrl, studyId, params);
  }

  public String infoSearch(@NonNull String studyId,
      final boolean includeInfo, @NonNull Iterable<String> searchTerms){
    val params = AMPERSAND_JOINER.join(searchTerms);
    return format("%s/studies/%s/analysis/search/info?includeInfo=%s&%s", serverUrl, studyId, includeInfo, params);
  }

}
