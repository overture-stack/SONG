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
 */
package org.icgc.dcc.sodalite.client.register;

import org.icgc.dcc.sodalite.client.config.SodaliteConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.val;

public class Registry {

  private final String serverUrl;
  private RestTemplate rest;
  ObjectMapper mapper;
  SodaliteConfig config;

  public Registry(SodaliteConfig config) {
    this.serverUrl = config.getServerUrl();
    this.rest = new RestTemplate();
    this.mapper = new ObjectMapper();
  }

  String analysisEndpoint(String studyId, String uploadId, String analysisType) {
    return String.format("%s/studies/%s/analyses/%s/%s", serverUrl, studyId, analysisType, uploadId);
  }

  String statusEndpoint(String studyId, String uploadId) {
    val url = "%s/studies/%s/statuses/%s";
    return String.format(url, serverUrl, studyId, uploadId);
  }

  @SneakyThrows
  String getAnalysisType(String json) {
    val node = mapper.readTree(json);

    if (node.has("sequencingRead")) {
      return "sequencingread";
    } else if (node.has("variantUpdateCall")) {
      return "variantcall";
    }
    throw new Error("Updated Analysis failed: unknown analysis type" + node.asText());
  }

  @SneakyThrows
  String getStudyId(String json) {
    val node = mapper.readTree(json);
    return node.get("study").get("studyId").asText();
  }

  @SneakyThrows
  /***
   * Register an analysis with the sodalite server.
   * 
   * @param uploadId
   * @param json
   * @return The analysisId that the server returned, or null if an error occurred.
   */
  public String registerAnalysis(String uploadId, String json) {
    val analysisType = getAnalysisType(json);
    val url = analysisEndpoint(getStudyId(json), uploadId, analysisType);
    val response = post(url, json);
    if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
      return response.getBody();
    }
    throw new Error(response.toString());
  }

  private ResponseEntity<String> post(String url, String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    HttpEntity<String> entity = new HttpEntity<String>(json, headers);
    val response = rest.postForEntity(url, entity, String.class);
    return response;
  }

  /***
   * Update an existing analysis object on the sodalite server
   * 
   * @param uploadId
   * @param json
   */
  public void updateAnalysis(String uploadId, String json) {
    val url = analysisEndpoint(getStudyId(json), uploadId, getAnalysisType(json));
    rest.put(url, json);
  }

  /***
   * Returns the state of the registration on the server (JSON)
   * 
   * @param uploadId
   * @return The state of the upload
   */
  public String getRegistrationState(String studyId, String uploadId) {
    val url = statusEndpoint(studyId, uploadId);
    val response = rest.getForEntity(url, String.class);
    if (response.getStatusCode() == HttpStatus.OK) {
      if (response.getBody() == null) {
        return String.format("Unknown uploadId '%s'\n", uploadId);
      }
      return response.getBody();
    }
    throw new Error(response.toString());
  }

}
