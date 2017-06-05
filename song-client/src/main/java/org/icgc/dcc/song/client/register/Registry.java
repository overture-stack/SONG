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

import org.icgc.dcc.sodalite.client.cli.Status;
import org.icgc.dcc.sodalite.client.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.val;

@Component
public class Registry {

  private RestClient rest;
  private ObjectMapper mapper;
  Endpoint endpoint;

  @Autowired
  public Registry(Config config) {
    this.mapper = new ObjectMapper();
    this.rest = new RestClient();
    this.endpoint = new Endpoint(config.getServerUrl());
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

  /***
   * Register an analysis with the sodalite server.
   * 
   * @param analysisId
   * @return The analysisId that the server returned, or null if an error occurred.
   */
  public Status upload(String json) {
    val url = endpoint.upload(getStudyId(json));
    return rest.post(url, json);
  }

  /***
   * Returns the state of the registration on the server (JSON)
   * 
   * @param uploadId
   * @return The state of the upload
   */
  public Status getUploadStatus(String studyId, String uploadId) {
    val url = endpoint.status(studyId, uploadId);
    return rest.get(url);
  }

  public Status publish(String studyId, String uploadId) {
    val url = endpoint.publishById(studyId, uploadId);
    return rest.post(url);
  }

  public Status getAnalysisFiles(String studyId, String analysisId) {
    val url = endpoint.getAnalysisFiles(studyId, analysisId);
    return rest.get(url);
  }

}
