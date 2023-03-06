/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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
package bio.overture.song.sdk;

import static bio.overture.song.core.utils.Booleans.convertToBoolean;

import bio.overture.song.core.exceptions.BooleanConversionException;
import bio.overture.song.core.model.Analysis;
import bio.overture.song.core.model.AnalysisType;
import bio.overture.song.core.model.ExportedPayload;
import bio.overture.song.core.model.FileDTO;
import bio.overture.song.core.model.FileUpdateRequest;
import bio.overture.song.core.model.FileUpdateResponse;
import bio.overture.song.core.model.PageDTO;
import bio.overture.song.core.model.SubmitResponse;
import bio.overture.song.sdk.model.ListAnalysisTypesRequest;
import bio.overture.song.sdk.web.Endpoint;
import bio.overture.song.sdk.web.RestClient;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.web.client.ResourceAccessException;

@RequiredArgsConstructor
public class SongApi {

  @NonNull private final RestClient restClient;
  @NonNull private final Endpoint endpoint;

  public SongApi(RestClient restClient) {
    this(restClient, new Endpoint());
  }

  /** Submit an payload to the song server. */
  public SubmitResponse submit(@NonNull String studyId, @NonNull String json) {
    val url = endpoint.submit(studyId);
    return restClient.post(url, json, SubmitResponse.class).getBody();
  }

  public List<FileDTO> getAnalysisFiles(@NonNull String studyId, @NonNull String analysisId) {
    val url = endpoint.getAnalysisFiles(studyId, analysisId);
    return restClient.getList(url, FileDTO.class).getBody();
  }

  public Analysis getAnalysis(@NonNull String studyId, @NonNull String analysisId) {
    val url = endpoint.getAnalysis(studyId, analysisId);
    return restClient.get(url, Analysis.class).getBody();
  }

  @SneakyThrows
  public PageDTO<AnalysisType> listAnalysisTypes(
      @NonNull ListAnalysisTypesRequest listAnalysisTypesRequest) {
    val url = endpoint.listAnalysisTypes(listAnalysisTypesRequest);
    return restClient.getPage(url, AnalysisType.class).getBody();
  }

  public AnalysisType registerAnalysisType(@NonNull String json) {
    val url = endpoint.registerAnalysisType();
    return restClient.post(url, json, AnalysisType.class).getBody();
  }

  /**
   * Returns true if the SONG server is running, otherwise false.
   *
   * @return boolean
   */
  public boolean isAlive() {
    val url = endpoint.isAlive();
    try {
      return convertToBoolean(restClient.get(url, String.class).getBody());
    } catch (BooleanConversionException | ResourceAccessException e) {
      return false;
    }
  }

  public String publish(
      @NonNull String studyId, @NonNull String analysisId, boolean ignoreUndefinedMd5) {
    val url = endpoint.publish(studyId, analysisId, ignoreUndefinedMd5);
    return restClient.put(url, String.class).getBody();
  }

  public String unpublish(@NonNull String studyId, @NonNull String analysisId) {
    val url = endpoint.unpublish(studyId, analysisId);
    return restClient.put(url, String.class).getBody();
  }

  public List<ExportedPayload> exportStudy(@NonNull String studyId) {
    val url = endpoint.exportStudy(studyId);
    return restClient.getList(url, ExportedPayload.class).getBody();
  }

  public List<ExportedPayload> exportAnalyses(@NonNull List<String> analysisIds) {
    val url = endpoint.exportAnalysisIds(analysisIds);
    return restClient.getList(url, ExportedPayload.class).getBody();
  }

  public String suppress(@NonNull String studyId, @NonNull String analysisId) {
    val url = endpoint.suppress(studyId, analysisId);
    return restClient.put(url, String.class).getBody();
  }

  public void updateAnalysis(
      @NonNull String studyId, @NonNull String analysisId, @NonNull String updateAnalysisRequest) {
    val url = endpoint.updateAnalysis(studyId, analysisId);
    restClient.put(url, updateAnalysisRequest, Object.class);
  }

  public void patchAnalysis(
      @NonNull String studyId, @NonNull String analysisId, @NonNull String patchAnalysisRequest) {
    val url = endpoint.patchAnalysis(studyId, analysisId);
    restClient.patch(url, patchAnalysisRequest, Object.class);
  }

  public FileUpdateResponse updateFile(
      @NonNull String studyId,
      @NonNull String objectId,
      @NonNull FileUpdateRequest fileUpdateRequest) {
    val url = endpoint.updateFile(studyId, objectId);
    return restClient.put(url, fileUpdateRequest, FileUpdateResponse.class).getBody();
  }

  public List<Analysis> idSearch(
      String studyId, String sampleId, String specimenId, String donorId, String fileId) {
    val url = endpoint.idSearch(studyId, sampleId, specimenId, donorId, fileId);
    return restClient.getList(url, Analysis.class).getBody();
  }

  public AnalysisType getAnalysisType(
      @NonNull String name, Integer version, Boolean unrenderedOnly) {
    val url = endpoint.getAnalysisType(name, version, unrenderedOnly);
    return restClient.get(url, AnalysisType.class).getBody();
  }
}
