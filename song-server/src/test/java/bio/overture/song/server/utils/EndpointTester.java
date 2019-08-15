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

package bio.overture.song.server.utils;

import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.LIMIT;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.OFFSET;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.SORT;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.SORTORDER;
import static bio.overture.song.server.utils.SongErrorResultMatcher.songErrorContent;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.utils.web.ResponseOption;
import bio.overture.song.server.utils.web.WebResource;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.common.core.util.Joiners;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@RequiredArgsConstructor
public class EndpointTester {

  private static final String SCHEMAS = "schemas";
  private static final String NAMES = "names";
  private static final String VERSIONS = "versions";
  private static final String META = "meta";
  private static final String HIDE_SCHEMA = "hideSchema";

  public static final Joiner AMPERSAND = Joiner.on("&");

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

  public WebResource initWebRequest() {
    val headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    headers.setAccept(ImmutableList.of(APPLICATION_JSON));
    return new WebResource(mockMvc, "").logging().headers(headers);
  }

  // GET /schemas
  public ResponseOption getSchemaGetRequestAnd(
      Collection<String> names,
      Collection<Integer> versions,
      Boolean hideSchema,
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
        .optionalQueryParamArray(SORT, sortVariables)
        .getAnd();
  }

  // POST /schemas
  public ResponseOption registerAnalysisTypePostRequestAnd(
      @NonNull RegisterAnalysisTypeRequest request) {
    return initWebRequest().endpoint(SCHEMAS).body(request).postAnd();
  }

  // GET /schemas/<name>:<version>
  public ResponseOption getAnalysisTypeVersionGetRequestAnd(@NonNull String analysisTypeIdString) {
    return initWebRequest().endpoint(Joiners.PATH.join(SCHEMAS, analysisTypeIdString)).getAnd();
  }

  // GET /schemas/meta
  public ResponseOption getMetaSchemaGetRequestAnd() {
    return initWebRequest().endpoint(Joiners.PATH.join(SCHEMAS, META)).getAnd();
  }

  public static EndpointTester createEndpointTester(MockMvc mockMvc, boolean enableLogging) {
    return new EndpointTester(mockMvc, enableLogging);
  }
}
