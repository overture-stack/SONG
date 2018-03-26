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

package org.icgc.dcc.song.client.benchmark.rest;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.springframework.http.HttpStatus;

import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.icgc.dcc.song.client.benchmark.rest.StudyClient.StudyEndpoint.createStudyEndpoint;

@RequiredArgsConstructor(access = PRIVATE)
public class StudyClient {

  private final String accessToken;
  private final StudyEndpoint endpoint;
  private final EntityRestClient restClient;

  @SneakyThrows
  public boolean isStudyExist(@NonNull String studyId){
    val response = restClient.get(accessToken, endpoint.getStudy(studyId));
    if (response.getStatusCode() == HttpStatus.NOT_FOUND){
      return false;
    } else {
      val node = JsonUtils.readTree(response.getBody());
      if (!node.isArray() || !node.hasNonNull(0)){
        return false;
      }
      return stream(node.elements()).collect(Collectors.toList()).size() > 0;
    }
  }

  public void saveStudy(@NonNull String id,
      @NonNull String name, @NonNull String org, @NonNull String desc  ){
    val json = JsonUtils.toJson(
        object()
            .with("studyId", id)
            .with("name", name)
            .with("organization", org)
            .with("description", desc)
            .end());

    val response = restClient.postAuth(accessToken, endpoint.saveStudy(id), json);
    checkState(!response.getStatusCode().is4xxClientError() && !response.getStatusCode()
        .is5xxServerError(), "An error occured");
  }

  public static StudyClient createStudyClient(@NonNull String accessToken, @NonNull String serverUrl){
    return new StudyClient(accessToken,createStudyEndpoint(serverUrl), new EntityRestClient());
  }

  @RequiredArgsConstructor
  public static class StudyEndpoint {

    @NonNull private final String serverUrl;

    private String getStudy(String studyId){
      return format("%s/studies/%s",serverUrl,studyId);
    }

    private String saveStudy(String studyId){
      return format("%s/studies/%s/",serverUrl,studyId);
    }

    public static StudyEndpoint createStudyEndpoint(String serverUrl) {
      return new StudyEndpoint(serverUrl);
    }

  }
}
