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
package bio.overture.song.sdk.register;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

import bio.overture.song.sdk.cli.Status;
import bio.overture.song.sdk.config.ClientConfig;
import bio.overture.song.sdk.register.Endpoint.ListAnalysisTypesRequest;
import bio.overture.song.core.model.FileData;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.springframework.web.client.RestClientException;

public class Registry {

  private final RestClient restClient;
  private final Endpoint endpoint;
  private final String studyId;
  private final String serverUrl;

  public Registry(@NonNull ClientConfig config, @NonNull RestClient restClient) {
    this.restClient = restClient;
    this.endpoint = new Endpoint();
    this.studyId = config.getStudyId();
    this.serverUrl = config.getServerUrl();
  }

  /** Submit an payload to the song server. */
  public Status submit(String json) {
    checkServerAlive();
    val url = endpoint.submit(studyId);
    return restClient.post(url, json);
  }

  public Status getAnalysisFiles(String studyId, String analysisId) {
    checkServerAlive();
    val url = endpoint.getAnalysisFiles(studyId, analysisId);
    return restClient.get(url);
  }

  public Status getAnalysis(String studyId, String analysisId) {
    checkServerAlive();
    val url = endpoint.getAnalysis(studyId, analysisId);
    return restClient.get(url);
  }

  public Status listAnalysisTypes(@NonNull ListAnalysisTypesRequest listAnalysisTypesRequest) {
    checkServerAlive();
    val url = endpoint.listAnalysisTypes(listAnalysisTypesRequest);
    return restClient.get(url);
  }

  public Status registerAnalysisType(@NonNull String json) {
    checkServerAlive();
    val url = endpoint.registerAnalysisType();
    return restClient.post(url, json);
  }

  /**
   * Returns true if the SONG server is running, otherwise false.
   *
   * @return boolean
   */
  public boolean isAlive() {
    val url = endpoint.isAlive();
    return parseBoolean(restClient.get(url).getOutputs());
  }

  /**
   * TODO: [DCC-5641] the ResponseEntity from AnalysisController is not returned, since
   * RestTemplate.put is a void method. need to find RestTemplate implementation that returns a
   * response
   */
  public Status publish(String studyId, String analysisId, boolean ignoreUndefinedMd5) {
    checkServerAlive();
    val url = endpoint.publish(studyId, analysisId, ignoreUndefinedMd5);
    return restClient.put(url);
  }

  public Status unpublish(String studyId, String analysisId) {
    checkServerAlive();
    val url = endpoint.unpublish(studyId, analysisId);
    return restClient.put(url);
  }

  public Status exportStudy(@NonNull String studyId, boolean includeAnalysisId) {
    checkServerAlive();
    val url = endpoint.exportStudy(studyId, includeAnalysisId);
    return restClient.get(url);
  }

  public Status exportAnalyses(@NonNull List<String> analysisIds, boolean includeAnalysisId) {
    checkServerAlive();
    val url = endpoint.exportAnalysisIds(analysisIds, includeAnalysisId);
    return restClient.get(url);
  }

  public Status suppress(String studyId, String analysisId) {
    checkServerAlive();
    val url = endpoint.suppress(studyId, analysisId);
    return restClient.put(url);
  }

  public Status updateAnalysis(String studyId, String analysisId, String updateAnalysisRequest) {
    checkServerAlive();
    val url = endpoint.updateAnalysis(studyId, analysisId);
    return restClient.putObject(url, updateAnalysisRequest);
  }

  public Status updateFile(String studyId, String objectId, FileData fileUpdateRequest) {
    checkServerAlive();
    val url = endpoint.updateFile(studyId, objectId);
    return restClient.putObject(url, fileUpdateRequest);
  }

  public Status idSearch(
      String studyId, String sampleId, String specimenId, String donorId, String fileId) {
    checkServerAlive();
    val url = endpoint.idSearch(studyId, sampleId, specimenId, donorId, fileId);
    return restClient.get(url);
  }

  public Status getAnalysisType(@NonNull String name, Integer version, Boolean unrenderedOnly) {
    checkServerAlive();
    val url = endpoint.getAnalysisType(name, version, unrenderedOnly);
    return restClient.get(url);
  }

  public void checkServerAlive() {
    if (!isAlive()) {
      throw new RestClientException(format("The song server '%s' is not reachable", serverUrl));
    }
  }
}
