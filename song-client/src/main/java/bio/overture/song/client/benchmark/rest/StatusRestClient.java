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

import bio.overture.song.client.cli.Status;
import bio.overture.song.client.errors.ServerResponseErrorHandler;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

public class StatusRestClient extends AbstractRestClient<Status>{

  private final RestTemplate restTemplate;

  public StatusRestClient() {
    this.restTemplate = new RestTemplate();
    this.restTemplate.setErrorHandler(new ServerResponseErrorHandler());
  }

  @Override
  protected <T> Status tryRequest(Function<RestTemplate, ResponseEntity<T>> restTemplateFunction) {
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
}
