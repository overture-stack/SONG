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
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.icgc.dcc.song.core.exceptions.SongError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import static java.lang.Boolean.parseBoolean;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SERVICE_UNAVAILABLE;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;
import static org.icgc.dcc.song.core.exceptions.SongError.createSongError;

@Component
public class Registry {

  private static final boolean DEFAULT_DEBUG_ENABLE = false;
  
  @Setter
  private RestClient restClient;
  private ObjectMapper mapper;
  private Endpoint endpoint;
  private String accessToken;
  private ErrorStatusHeader errorStatusHeader;

  @Autowired
  public Registry(Config config, RestClient restClient, ErrorStatusHeader errorStatusHeader) {
    this.mapper = new ObjectMapper();
    this.restClient = restClient;
    this.endpoint = new Endpoint(config.getServerUrl());
    this.accessToken = config.getAccessToken();
    this.errorStatusHeader = errorStatusHeader;
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
    try{
      return restClient.get(accessToken, url);
    } catch(ResourceAccessException e){
      val status = isServerAlive();
      try {
        val value = parseBoolean(status.toString());
        val songError = SongError.createSongError(UNKNOWN_ERROR, "Unknown error");
        throw new ServerException(songError,e);
      } catch (Exception e2){
        return status;
      }
    }
  }

  public Status save(String studyId, String uploadId) {
    val url = endpoint.saveById(studyId, uploadId);
    return restClient.postAuth(accessToken, url);
  }

  public Status getAnalysisFiles(String studyId, String analysisId) {
    val url = endpoint.getAnalysisFiles(studyId, analysisId);
    return restClient.get(accessToken, url);
  }

  public boolean isServerAlive2(){
    val url = endpoint.isAlive();
    try {
      return parseBoolean(restClient.get(url).toString());
    } catch (Throwable e){
      return false;
    }
  }

  public Status isServerAlive(){
    val url = endpoint.isAlive();
    try {
      return restClient.get(url);
    } catch (Throwable e){
      val songError = createSongError(SERVICE_UNAVAILABLE, e.getMessage());
      val status = new Status();
      status.err(errorStatusHeader.getSongClientErrorOutput(songError));
      return status;
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

  public Status search(String studyId,
      String sampleId,
      String specimenId,
      String donorId,
      String fileId){
    val url = endpoint.searchGet(studyId,sampleId,specimenId,donorId,fileId);
    return restClient.get(accessToken, url);
  }

}
