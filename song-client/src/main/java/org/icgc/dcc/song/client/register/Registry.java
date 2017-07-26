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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.client.cli.Status;
import org.icgc.dcc.song.client.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Registry {

  private static final boolean DEFAULT_DEBUG_ENABLE = false;

  @Setter
  private RestClient restClient;

  private ObjectMapper mapper;
  Endpoint endpoint;

  @Autowired
  public Registry(Config config, RestClient restClient) {
    this.mapper = new ObjectMapper();
    this.restClient = restClient;
    this.endpoint = new Endpoint(config.getServerUrl());
  }


  @SneakyThrows
  String getAnalysisType(String json) {
    val node = mapper.readTree(json);

    if (node.has("analysisType")) {
      return node.get("analysisType").asText();
    }
    throw new Error("No analysis type specified in JSON document" + node.asText());
  }

  @SneakyThrows
  String getStudyId(String json) {
    val node = mapper.readTree(json);
    return node.get("study").asText();
  }


  /**
   * Register an analysis with the song server.
   *
   * @param json
   * @return The analysisId that the server returned, or null if an error occurred.
   */
  public Status upload(String json, boolean isAsyncValidation) {
    val url = endpoint.upload(getStudyId(json), isAsyncValidation);
    return restClient.post(url, json);
  }

  /***
   * Returns the state of the registration on the server (JSON)
   * 
   * @param uploadId
   * @return The state of the upload
   */
  public Status getUploadStatus(String studyId, String uploadId) {
    val url = endpoint.status(studyId, uploadId);
    return restClient.get(url);
  }

  public Status save(String studyId, String uploadId) {
    val url = endpoint.saveById(studyId, uploadId);
    return restClient.post(url);
  }

  public Status getAnalysisFiles(String studyId, String analysisId) {
    val url = endpoint.getAnalysisFiles(studyId, analysisId);
    return restClient.get(url);
  }

  /**
   * TODO: [DCC-5641] the ResponseEntity from AnalysisController is not returned, since RestTemplate.put is a void method.
   * need to find RestTemplate implementation that returns a response
   */
  public Status publish(String studyId, String analysisId ){
    val url = endpoint.publish(studyId, analysisId);
    val status = restClient.put(url);
    if (!status.hasErrors() && !status.hasOutputs()){
      status.output("The analysisId '%s' was successfully published for the studyId '%s'", analysisId, studyId);
    }
    return status;
  }

  public Status search(String studyId,
      String sampleId,
      String specimenId,
      String donorId,
      String fileId){
    val url = endpoint.searchGet(studyId,sampleId,specimenId,donorId,fileId);
    return restClient.get(url);
  }

}
