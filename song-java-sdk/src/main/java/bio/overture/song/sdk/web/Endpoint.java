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
package bio.overture.song.sdk.web;

import static bio.overture.song.core.utils.Separators.COMMA;
import static java.lang.String.format;

import bio.overture.song.core.web.RequestParamBuilder;
import bio.overture.song.sdk.model.ListAnalysisTypesRequest;
import java.util.List;
import lombok.NonNull;

public class Endpoint {

  private final String serverUrl;

  public Endpoint(@NonNull String serverUrl) {
    this.serverUrl = sanitizeServerUrl(serverUrl);
  }

  public Endpoint() {
    this.serverUrl = "";
  }

  public String submit(@NonNull String studyId) {
    return format("%s/submit/%s", serverUrl, studyId);
  }

  public String getAnalysisFiles(@NonNull String studyId, @NonNull String analysisId) {
    return format("%s/studies/%s/analysis/%s/files", serverUrl, studyId, analysisId);
  }

  public String registerAnalysisType() {
    return format("%s/schemas", serverUrl);
  }

  public String listAnalysisTypes(@NonNull ListAnalysisTypesRequest r) {
    return new RequestParamBuilder()
        .optionalQuerySingleParam("hideSchema", r.getHideSchema())
        .optionalQuerySingleParam("limit", r.getLimit())
        .optionalQuerySingleParam("offset", r.getOffset())
        .optionalQueryParamCollection("names", r.getNames())
        .optionalQueryParamCollection("versions", r.getVersions())
        .optionalQueryParamCollection("sort", r.getSortOrders())
        .optionalQuerySingleParam("sortOrder", r.getSortDirection())
        .optionalQuerySingleParam("unrenderedOnly", r.getUnrenderedOnly())
        .build(format("%s/schemas", serverUrl));
  }

  public String getAnalysisType(@NonNull String name, Integer version, Boolean unrenderedOnly) {
    return new RequestParamBuilder()
        .optionalQuerySingleParam("version", version)
        .optionalQuerySingleParam("unrenderedOnly", unrenderedOnly)
        .build(serverUrl + "/schemas/" + name);
  }

  public String getAnalysis(String studyId, @NonNull String analysisId) {
    return format("%s/studies/%s/analysis/%s", serverUrl, studyId, analysisId);
  }

  public String isAlive() {
    return serverUrl + "/isAlive";
  }

  public String publish(
      @NonNull String studyId, @NonNull String analysisId, boolean ignoreUndefinedMd5) {
    return format(
        "%s/studies/%s/analysis/publish/%s?ignoreUndefinedMd5=%s",
        serverUrl, studyId, analysisId, ignoreUndefinedMd5);
  }

  public String unpublish(@NonNull String studyId, @NonNull String analysisId) {
    return format("%s/studies/%s/analysis/unpublish/%s", serverUrl, studyId, analysisId);
  }

  public String updateAnalysis(@NonNull String studyId, @NonNull String analysisId) {
    return format("%s/studies/%s/analysis/%s", serverUrl, studyId, analysisId);
  }

  public String updateFile(@NonNull String studyId, @NonNull String objectId) {
    return format("%s/studies/%s/files/%s", serverUrl, studyId, objectId);
  }

  public String exportAnalysisIds(@NonNull List<String> analysisIds) {
    return format("%s/export/analysis/%s", serverUrl, COMMA.join(analysisIds));
  }

  public String exportStudy(@NonNull String studyId) {
    return format("%s/export/studies/%s", serverUrl, studyId);
  }

  public String suppress(@NonNull String studyId, @NonNull String analysisId) {
    return format("%s/studies/%s/analysis/suppress/%s", serverUrl, studyId, analysisId);
  }

  public String idSearch(
      @NonNull String studyId, String sampleId, String specimenId, String donorId, String fileId) {
    return new RequestParamBuilder()
        .optionalQuerySingleParam("sampleId", sampleId)
        .optionalQuerySingleParam("specimenId", specimenId)
        .optionalQuerySingleParam("donorId", donorId)
        .optionalQuerySingleParam("fileId", fileId)
        .build(format("%s/studies/%s/analysis/search/id", serverUrl, studyId));
  }

  private static String sanitizeServerUrl(String serverUrl) {
    return serverUrl.trim().replaceAll("[\\/]+$", "");
  }
}
