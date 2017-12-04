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

import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.icgc.dcc.song.client.cli.Status;
import org.icgc.dcc.song.client.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Registry {

  @Setter
  private final RestClient restClient;
  private final Endpoint endpoint;
  private final String accessToken;
  private final String studyId;

  @Autowired
  public Registry(@NonNull Config config, @NonNull RestClient restClient) {
    this.restClient = restClient;
    this.endpoint = new Endpoint(config.getServerUrl());
    this.accessToken = config.getAccessToken();
    this.studyId = config.getStudyId();
  }

  /**
   * Register an analysis with the song server.
   *
   * @param json
   * @return The analysisId that the server returned, or null if an error occurred.
   */
  public Status upload(String json, boolean isAsyncValidation) {
    val url = endpoint.upload(studyId, isAsyncValidation);
    return restClient.postAuth(accessToken, url, json);
  }

  /***
   * Returns the state of the registration on the server (JSON)
   *
   * @param uploadId
   * @return The state of the upload
   */
  public Status getUploadStatus(String studyId, String uploadId) {
    val url = endpoint.status(studyId, uploadId);
    return restClient.get(accessToken, url);
  }

  public Status save(String studyId, String uploadId, boolean ignoreAnalysisIdCollisions) {
    val url = endpoint.saveById(studyId, uploadId, ignoreAnalysisIdCollisions);
    return restClient.postAuth(accessToken, url);
  }

  public Status getAnalysisFiles(String studyId, String analysisId) {
    val url = endpoint.getAnalysisFiles(studyId, analysisId);
    return restClient.get(accessToken, url);
  }

  public Status getAnalysis(String studyId, String analysisId) {
    val url = endpoint.getAnalysis(studyId, analysisId);
    return restClient.get(accessToken, url);
  }

  /**
   * Returns true if the SONG server is running, otherwise false.
   * @return boolean
   */
  public boolean isAlive(){
    val url = endpoint.isAlive();
    try {
      return Boolean.parseBoolean(restClient.get(url).getOutputs());
    } catch (Throwable e){
      return false;
    }
  }

  /**
   * TODO: [DCC-5641] the ResponseEntity from AnalysisController is not returned, since RestTemplate.put is a void method.
   * need to find RestTemplate implementation that returns a response
   */
  public Status publish(String studyId, String analysisId ){
    val url = endpoint.publish(studyId, analysisId);
    return restClient.putAuth(accessToken, url);
  }

  public Status suppress(String studyId, String analysisId ){
    val url = endpoint.suppress(studyId, analysisId);
    return restClient.putAuth(accessToken, url);
  }

  public Status idSearch(String studyId,
      String sampleId,
      String specimenId,
      String donorId,
      String fileId){
    val url = endpoint.idSearch(studyId,sampleId,specimenId,donorId,fileId);
    return restClient.get(accessToken, url);
  }

  public Status infoSearch(String studyId, boolean includeInfo, Iterable<String> searchTerms){
    val url = endpoint.infoSearch(studyId,includeInfo, searchTerms);
    return restClient.get(accessToken, url);
  }

}
