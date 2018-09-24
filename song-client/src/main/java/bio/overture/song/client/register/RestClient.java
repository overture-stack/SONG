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

import lombok.NonNull;
import lombok.val;
import bio.overture.song.client.cli.Status;
import bio.overture.song.client.errors.ServerResponseErrorHandler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

import static java.util.Objects.isNull;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@Component
public class RestClient {

  private final RestTemplate restTemplate;

  public RestClient() {
    this.restTemplate = new RestTemplate();
    this.restTemplate.setErrorHandler(new ServerResponseErrorHandler());
  }

  public Status get(@NonNull String url) {
    return _get(buildJsonContentHttpHeader(),url);
  }

  public Status get(@NonNull String accessToken, String url) {
    return _get(buildAuthHttpHeader(accessToken), url);
  }

  public Status post(@NonNull String url, String json) {
    return _post(buildJsonContentHttpHeader(), url, json);
  }

  public Status postAuth(@NonNull String accessToken, String url, String json) {
    return _post(buildAuthJsonContentHttpHeader(accessToken), url, json);
  }

  public Status postAuth(@NonNull String accessToken, String url) {
    return _post(buildAuthJsonContentHttpHeader(accessToken), url, "");
  }

  public Status post(@NonNull String url) {
    return post(url, "");
  }


  public Status putObject(String url,  Object o) {
    return put(url, toJson(o));
  }

  public Status put(String url, String json) {
    return _put(buildJsonContentHttpHeader(), url, json);
  }

  public Status put(String url) {
    return put(url,"");
  }

  public Status putAuthObject(@NonNull String accessToken,  String url,  Object o) {
    return putAuth(accessToken, url, toJson(o));
  }

  public Status putAuth(@NonNull String accessToken, String url, String json) {
    return _put(buildAuthJsonContentHttpHeader(accessToken), url, json);
  }

  public Status putAuth(@NonNull String accessToken, String url) {
    return putAuth(accessToken, url,"");
  }

  private <T> Status tryRequest(Function<RestTemplate, ResponseEntity<T>> restTemplateFunction){
    Status status = new Status();
    val response = restTemplateFunction.apply(restTemplate);
    if (response.getStatusCode() == HttpStatus.OK) {
      if (response.getBody() == null) {
        status.err("[SONG_CLIENT_ERROR]: Null response from server: %s", response.toString());
      } else {
        status.output(response.getBody().toString());
      }
    } else {
      status.err("[%s]: %s",response.getStatusCode().value(),response.toString());
    }
    return status;
  }


  private Status _get(@NonNull HttpHeaders httpHeaders, @NonNull String url) {
    val entity = new HttpEntity<String>(null, httpHeaders);
    return tryRequest(x -> x.exchange(url, GET, entity, String.class));
  }

  private Status _put(@NonNull HttpHeaders httpHeaders, @NonNull String url, String json) {
    val entity = new HttpEntity<String>(json, httpHeaders);
    return tryRequest(x -> x.exchange(url, PUT, entity, String.class));
  }

  private Status _post(@NonNull HttpHeaders httpHeaders, @NonNull String url, String json){
    val entity = new HttpEntity<String>(json, httpHeaders);
    return tryRequest(x -> x.postForEntity(url, entity, String.class));
  }

  private static HttpHeaders buildJsonContentHttpHeader(){
    return buildHttpHeader(null, true);
  }

  private static HttpHeaders buildAuthHttpHeader(String accessToken ){
    return buildHttpHeader(accessToken, false);
  }

  private static HttpHeaders buildAuthJsonContentHttpHeader(String accessToken ){
    return buildHttpHeader(accessToken, true);
  }

  private static HttpHeaders buildHttpHeader(String accessToken, boolean isJsonContent ){
    val headers = new HttpHeaders();
    if (!isNull(accessToken)){
      headers.set(AUTHORIZATION, "Bearer "+accessToken);
    }
    if (isJsonContent){
      headers.setContentType(APPLICATION_JSON_UTF8);
    }
    return headers;
  }

}
