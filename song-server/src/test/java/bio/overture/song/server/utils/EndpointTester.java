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

package bio.overture.song.server.utils;

import static bio.overture.song.server.model.enums.ModelAttributeNames.LIMIT;
import static bio.overture.song.server.model.enums.ModelAttributeNames.OFFSET;
import static bio.overture.song.server.model.enums.ModelAttributeNames.SORT;
import static bio.overture.song.server.model.enums.ModelAttributeNames.SORTORDER;
import static bio.overture.song.server.utils.SongErrorResultMatcher.songErrorContent;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.core.utils.Joiners;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.utils.web.MockMvcWebResource;
import bio.overture.song.server.utils.web.ResponseOption;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.test.web.servlet.MockMvc;

@RequiredArgsConstructor
public class EndpointTester {

  private static final String SCHEMAS = "schemas";
  private static final String NAMES = "names";
  private static final String VERSIONS = "versions";
  private static final String VERSION = "version";
  private static final String META = "meta";
  private static final String HIDE_SCHEMA = "hideSchema";
  private static final String UNRENDERED_ONLY = "unrenderedOnly";

  @NonNull private final MockMvc mockMvc;
  private final boolean enableLogging;

  @SneakyThrows
  public void testPostError(
      @NonNull String endpointPath,
      @NonNull String payload,
      @NonNull ServerError expectedServerError) {
    this.mockMvc
        .perform(
            post(endpointPath)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content(payload))
        .andExpect(songErrorContent(expectedServerError));
  }

  public MockMvcWebResource initWebRequest() {
    val headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setAccept(ImmutableList.of(APPLICATION_JSON));
    val w = new MockMvcWebResource("", mockMvc).headers(headers);
    if (enableLogging) {
      w.logging();
    }
    return w;
  }

  public ResponseOption listSchemasGetRequestAnd(
      Collection<String> names,
      Collection<Integer> versions,
      Boolean hideSchema,
      Integer offset,
      Integer limit,
      Sort.Direction sortOrder,
      String... sortVariables) {
    return listSchemasGetRequestAnd(
        names, versions, hideSchema, false, offset, limit, sortOrder, sortVariables);
  }

  // GET /schemas
  public ResponseOption listSchemasGetRequestAnd(
      Collection<String> names,
      Collection<Integer> versions,
      Boolean hideSchema,
      Boolean unrenderedOnly,
      Integer offset,
      Integer limit,
      Sort.Direction sortOrder,
      String... sortVariables) {
    return initWebRequest()
        .endpoint(SCHEMAS)
        .optionalQueryParamCollection(NAMES, names)
        .optionalQueryParamCollection(VERSIONS, versions)
        .optionalQuerySingleParam(OFFSET, offset)
        .optionalQuerySingleParam(HIDE_SCHEMA, hideSchema)
        .optionalQuerySingleParam(LIMIT, limit)
        .optionalQuerySingleParam(SORTORDER, sortOrder)
        .optionalQuerySingleParam(UNRENDERED_ONLY, unrenderedOnly)
        .optionalQueryParamArray(SORT, sortVariables)
        .getAnd();
  }

  // POST /schemas
  public ResponseOption registerAnalysisTypePostRequestAnd(
      @NonNull RegisterAnalysisTypeRequest request) {
    return initWebRequest().endpoint(SCHEMAS).body(request).postAnd();
  }

  public ResponseOption updateAnalysisPutRequestAnd(
      @NonNull String studyId,
      @NonNull String analysisId,
      @NonNull JsonNode updateAnalysisRequest) {
    return initWebRequest()
        .endpoint("studies/%s/analysis/%s", studyId, analysisId)
        .body(updateAnalysisRequest)
        .putAnd();
  }

  // GET /schemas/<name>
  public ResponseOption getLatestAnalysisTypeGetRequestAnd(@NonNull String analysisTypeName) {
    return getAnalysisTypeVersionGetRequestAnd(analysisTypeName, null, false);
  }

  // POST /upload/{study}
  public ResponseOption submitPostRequestAnd(@NonNull String studyId, JsonNode payload) {
    return initWebRequest().endpoint("upload/%s", studyId).body(payload).postAnd();
  }

  // PUT /studies/{}/analysis/publish/{aid}
  public ResponseOption publishAnalysisPutRequestAnd(
      @NonNull String studyId, @NonNull String analysisId) {
    return initWebRequest()
        .endpoint("studies/%s/analysis/publish/%s", studyId, analysisId)
        .putAnd();
  }

  // GET /schemas/<name>?version=<integer>&unrenderedOnly=<boolean>
  public ResponseOption getAnalysisTypeVersionGetRequestAnd(
      @NonNull String analysisTypeName, @Nullable Integer version, boolean unrenderedOnly) {
    return initWebRequest()
        .endpoint(Joiners.PATH.join(SCHEMAS, analysisTypeName))
        .optionalQuerySingleParam(VERSION, version)
        .optionalQuerySingleParam(UNRENDERED_ONLY, unrenderedOnly)
        .getAnd();
  }

  // GET /schemas/meta
  public ResponseOption getMetaSchemaGetRequestAnd() {
    return initWebRequest().endpoint(Joiners.PATH.join(SCHEMAS, META)).getAnd();
  }

  public static EndpointTester createEndpointTester(MockMvc mockMvc, boolean enableLogging) {
    return new EndpointTester(mockMvc, enableLogging);
  }
}
