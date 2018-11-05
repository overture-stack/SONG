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
package bio.overture.song.client.register;

import bio.overture.song.client.cli.Status;
import bio.overture.song.client.config.Config;
import bio.overture.song.core.model.file.FileData;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

@Component
public class Registry {

  private final RestClient restClient;
  private final Endpoint endpoint;
  private final String accessToken;
  private final String studyId;
  private final String serverUrl;

  @Autowired
  public Registry(@NonNull Config config, @NonNull RestClient restClient) {
    this.restClient = restClient;
    this.endpoint = new Endpoint(config.getServerUrl());
    this.accessToken = config.getAccessToken();
    this.studyId = config.getStudyId();
    this.serverUrl = config.getServerUrl();
  }

  /**
   * Register an analysis with the song server.
   *
   * @param json
   * @return The analysisId that the server returned, or null if an error occurred.
   */
  public Status upload(String json, boolean isAsyncValidation) {
    checkServerAlive();
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
    checkServerAlive();
    val url = endpoint.status(studyId, uploadId);
    return restClient.get(accessToken, url);
  }

  public Status save(String studyId, String uploadId, boolean ignoreAnalysisIdCollisions) {
    checkServerAlive();
    val url = endpoint.saveById(studyId, uploadId, ignoreAnalysisIdCollisions);
    return restClient.postAuth(accessToken, url);
  }

  public Status getAnalysisFiles(String studyId, String analysisId) {
    checkServerAlive();
    val url = endpoint.getAnalysisFiles(studyId, analysisId);
    return restClient.get(accessToken, url);
  }

  public Status getAnalysis(String studyId, String analysisId) {
    checkServerAlive();
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
      return parseBoolean(restClient.get(url).getOutputs());
    } catch (Throwable e){
      return false;
    }
  }

  /**
   * TODO: [DCC-5641] the ResponseEntity from AnalysisController is not returned, since RestTemplate.put is a void method.
   * need to find RestTemplate implementation that returns a response
   */
  public Status publish(String studyId, String analysisId, boolean ignoreUndefinedMd5 ){
    checkServerAlive();
    val url = endpoint.publish(studyId, analysisId, ignoreUndefinedMd5);
    return restClient.putAuth(accessToken, url);
  }

  public Status unpublish(String studyId, String analysisId){
    checkServerAlive();
    val url = endpoint.unpublish(studyId, analysisId);
    return restClient.putAuth(accessToken, url);
  }

  public Status exportStudy(@NonNull String studyId, boolean includeAnalysisId){
    checkServerAlive();
    val url = endpoint.exportStudy(studyId, includeAnalysisId);
    return restClient.get(url);
  }

  public Status exportAnalyses(@NonNull List<String> analysisIds, boolean includeAnalysisId){
    checkServerAlive();
    val url = endpoint.exportAnalysisIds(analysisIds, includeAnalysisId);
    return restClient.get(url);
  }

  public Status suppress(String studyId, String analysisId ){
    checkServerAlive();
    val url = endpoint.suppress(studyId, analysisId);
    return restClient.putAuth(accessToken, url);
  }

  public Status updateFile(String studyId, String objectId, FileData fileUpdateRequest){
    checkServerAlive();
    val url = endpoint.updateFile(studyId, objectId);
    return restClient.putAuthObject(accessToken, url, fileUpdateRequest);
  }

  public Status idSearch(String studyId,
      String sampleId,
      String specimenId,
      String donorId,
      String fileId){
    checkServerAlive();
    val url = endpoint.idSearch(studyId,sampleId,specimenId,donorId,fileId);
    return restClient.get(accessToken, url);
  }

  public Status infoSearch(String studyId, boolean includeInfo, Iterable<String> searchTerms){
    checkServerAlive();
    val url = endpoint.infoSearch(studyId,includeInfo, searchTerms);
    return restClient.get(accessToken, url);
  }

  public void checkServerAlive(){
    if (!isAlive()){
      throw new RestClientException(format("The song server '%s' is not reachable", serverUrl));
    }
  }

}
