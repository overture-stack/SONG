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

package bio.overture.song.client.benchmark.rest;

import bio.overture.song.client.errors.ServerResponseErrorHandler;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

import static org.springframework.http.ResponseEntity.status;

public class EntityRestClient extends AbstractRestClient<ResponseEntity<String>>{

  private final RestTemplate restTemplate;

  public EntityRestClient() {
    this.restTemplate = new RestTemplate();
    this.restTemplate.setErrorHandler(new ServerResponseErrorHandler());
  }

  @Override
  protected <T> ResponseEntity<String> tryRequest(
      Function<RestTemplate, ResponseEntity<T>> restTemplateFunction) {
    val response = restTemplateFunction.apply(restTemplate);
    return status(response.getStatusCode()).body(response.getBody().toString());
  }

}
