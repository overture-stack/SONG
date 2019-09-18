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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.lang.Nullable;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;

@RequiredArgsConstructor
public class Endpoint {

  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  @NonNull private String serverUrl;

  public String submit(String studyId) {
    return format("%s/upload/%s", serverUrl, studyId);
  }

  public String getAnalysisFiles(String studyId, String analysisId) {
    return format("%s/studies/%s/analysis/%s/files", serverUrl, studyId, analysisId);
  }

  public String getAnalysisType(@NonNull String name, @Nullable Integer version, @Nullable Boolean unrenderedOnly) {
    val sb = new StringBuilder();
    sb.append(format("%s/schemas/%s", serverUrl, name));
    if (!isNull(version) && !isNull(unrenderedOnly) ){
      sb.append("?")
          .append("version="+version)
          .append("&")
          .append("unrenderedOnly="+unrenderedOnly);
    } else if(!isNull(version)){
      sb.append("?")
          .append("version="+version);
    } else if(!isNull(unrenderedOnly)){
      sb.append("?")
          .append("unrenderedOnly="+unrenderedOnly);
    }
    return sb.toString();
  }

  public String getAnalysis(String studyId, String analysisId) {
    return format("%s/studies/%s/analysis/%s", serverUrl, studyId, analysisId);
  }

  public String isAlive() {
    return format("%s/isAlive", serverUrl);
  }

  public String publish(String studyId, String analysisId, boolean ignoreUndefinedMd5) {
    return format(
        "%s/studies/%s/analysis/publish/%s?ignoreUndefinedMd5=%s",
        serverUrl, studyId, analysisId, ignoreUndefinedMd5);
  }

  public String unpublish(String studyId, String analysisId) {
    return format("%s/studies/%s/analysis/unpublish/%s", serverUrl, studyId, analysisId);
  }

  public String updateFile(String studyId, String objectId) {
    return format("%s/studies/%s/files/%s", serverUrl, studyId, objectId);
  }

  public String exportAnalysisIds(List<String> analysisIds, boolean includeAnalysisId) {
    return format(
        "%s/export/analysis/%s?includeAnalysisId=%s",
        serverUrl, COMMA.join(analysisIds), includeAnalysisId);
  }

  public String exportStudy(String studyId, boolean includeAnalysisId) {
    return format(
        "%s/export/studies/%s?includeAnalysisId=%s", serverUrl, studyId, includeAnalysisId);
  }

  public String suppress(String studyId, String analysisId) {
    return format("%s/studies/%s/analysis/suppress/%s", serverUrl, studyId, analysisId);
  }

  public String idSearch(
      @NonNull String studyId, String sampleId, String specimenId, String donorId, String fileId) {
    val list = Lists.<String>newArrayList();
    if (!isNull(sampleId)) {
      list.add("sampleId=" + sampleId);
    }
    if (!isNull(specimenId)) {
      list.add("specimenId=" + specimenId);
    }
    if (!isNull(donorId)) {
      list.add("donorId=" + donorId);
    }
    if (!isNull(fileId)) {
      list.add("fileId=" + fileId);
    }
    val params = AMPERSAND_JOINER.join(list);
    return format("%s/studies/%s/analysis/search/id?%s", serverUrl, studyId, params);
  }

  public String infoSearch(
      @NonNull String studyId, final boolean includeInfo, @NonNull Iterable<String> searchTerms) {
    val params = AMPERSAND_JOINER.join(searchTerms);
    return format(
        "%s/studies/%s/analysis/search/info?includeInfo=%s&%s",
        serverUrl, studyId, includeInfo, params);
  }

  public String getSchema(@NonNull String schemaId) {
    return format("%s/schema/%s", serverUrl, schemaId);
  }

  public String listSchemas() {
    return format("%s/schema/list", serverUrl);
  }
}
