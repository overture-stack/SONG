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
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static bio.overture.song.core.utils.JsonUtils.toJson;

@Component
public class RestClient2 {

  private final RestTemplate restTemplate;
  private final RetryTemplate retryTemplate;

  @Autowired
  public RestClient2(@NonNull RestTemplate restTemplate, @NonNull RetryTemplate retryTemplate) {
    this.restTemplate = restTemplate;
    this.retryTemplate = retryTemplate;
  }

  public Status get(@NonNull String endpoint) {
    return tryRequest(x -> x.exchange(endpoint, GET, null, String.class));
  }

  public Status post(@NonNull String endpoint, String json) {
    val entity = new HttpEntity<String>(json, null);
    return tryRequest(x -> x.postForEntity(endpoint, entity, String.class));
  }

  public Status post(@NonNull String endpoint) {
    return post(endpoint, "");
  }

  public Status putObject(String endpoint, Object o) {
    return put(endpoint, toJson(o));
  }

  public Status put(String endpoint, String json) {
    val entity = new HttpEntity<String>(json, null);
    return tryRequest(x -> x.exchange(endpoint, PUT, entity, String.class));
  }

  public Status put(String endpoint) {
    return put(endpoint, "");
  }

  private <T> Status tryRequest(Function<RestTemplate, ResponseEntity<T>> restTemplateFunction) {
    Status status = new Status();
    val response = retryTemplate.execute(r -> restTemplateFunction.apply(restTemplate));
    if (response.getStatusCode() == HttpStatus.OK) {
      if (response.getBody() == null) {
        status.err("[SONG_CLIENT_ERROR]: Null response from server: %s", response.toString());
      } else {
        status.output(response.getBody().toString());
      }
    } else {
      status.err("[%s]: %s", response.getStatusCode().value(), response.toString());
    }
    return status;
  }
}
