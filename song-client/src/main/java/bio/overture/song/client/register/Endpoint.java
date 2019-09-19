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

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;

import bio.overture.song.client.command.ListAnalysisTypesCommand.SortDirection;
import bio.overture.song.client.command.ListAnalysisTypesCommand.SortOrder;
import bio.overture.song.client.util.RequestParamBuilder;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public class Endpoint {

  public String submit(String studyId) {
    return format("/upload/%s", studyId);
  }

  public String getAnalysisFiles(String studyId, String analysisId) {
    return format("/studies/%s/analysis/%s/files", studyId, analysisId);
  }

  public String registerAnalysisType() {
    return "/schemas";
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
        .build("/schemas");
  }

  public String getAnalysisType(
      @NonNull String name, @Nullable Integer version, @Nullable Boolean unrenderedOnly) {
    return new RequestParamBuilder()
        .optionalQuerySingleParam("version", version)
        .optionalQuerySingleParam("unrenderedOnly", unrenderedOnly)
        .build("/schemas/" + name);
  }

  public String getAnalysis(String studyId, String analysisId) {
    return format("/studies/%s/analysis/%s", studyId, analysisId);
  }

  public String isAlive() {
    return "/isAlive";
  }

  public String publish(String studyId, String analysisId, boolean ignoreUndefinedMd5) {
    return format(
        "/studies/%s/analysis/publish/%s?ignoreUndefinedMd5=%s",
        studyId, analysisId, ignoreUndefinedMd5);
  }

  public String unpublish(String studyId, String analysisId) {
    return format("/studies/%s/analysis/unpublish/%s", studyId, analysisId);
  }

  public String updateAnalysis(String studyId, String analysisId) {
    return format("/studies/%s/analysis/%s", studyId, analysisId);
  }

  public String updateFile(String studyId, String objectId) {
    return format("/studies/%s/files/%s", studyId, objectId);
  }

  public String exportAnalysisIds(List<String> analysisIds, boolean includeAnalysisId) {
    return format(
        "/export/analysis/%s?includeAnalysisId=%s", COMMA.join(analysisIds), includeAnalysisId);
  }

  public String exportStudy(String studyId, boolean includeAnalysisId) {
    return format("/export/studies/%s?includeAnalysisId=%s", studyId, includeAnalysisId);
  }

  public String suppress(String studyId, String analysisId) {
    return format("/studies/%s/analysis/suppress/%s", studyId, analysisId);
  }

  public String idSearch(
      @NonNull String studyId, String sampleId, String specimenId, String donorId, String fileId) {
    return new RequestParamBuilder()
        .optionalQuerySingleParam("sampleId", sampleId)
        .optionalQuerySingleParam("specimenId", specimenId)
        .optionalQuerySingleParam("donorId", donorId)
        .optionalQuerySingleParam("fileId", fileId)
        .build(format("/studies/%s/analysis/search/id", studyId));
  }

  public String getSchema(@NonNull String schemaId) {
    return format("/schema/%s", schemaId);
  }

  public String listSchemas() {
    return "/schema/list";
  }

  @Value
  @Builder
  public static class ListAnalysisTypesRequest {
    @NonNull private final List<String> names;
    @NonNull private final List<Integer> versions;
    @NonNull private final List<SortOrder> sortOrders;

    /** Nullable */
    private final Integer offset;

    private final Integer limit;
    private final Boolean hideSchema;
    private final Boolean unrenderedOnly;
    private final SortDirection sortDirection;
  }
}
