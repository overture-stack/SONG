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

package bio.overture.song.server.service.id;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryOperations;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import java.util.Optional;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.REST_CLIENT_UNEXPECTED_RESPONSE;
import static bio.overture.song.core.exceptions.ServerException.checkServer;

@RequiredArgsConstructor
public class RestClient {

  /** Dependencies */
  @NonNull private final RestOperations rest;

  @NonNull private final RetryOperations retry;

  public Optional<String> getString(String url) {
    return getObject(url, String.class);
  }

  public <T> Optional<T> getObject(@NonNull String url, @NonNull Class<T> responseType) {
    try {
      val response = get(url, responseType);
      checkServer(
          !isNull(response),
          getClass(),
          REST_CLIENT_UNEXPECTED_RESPONSE,
          "The request 'GET %s' returned a NULL response, "
              + "but was expecting a 200 status code of type '%s'",
          url,
          responseType.getSimpleName());
      checkServer(
          !isNull(response.getBody()),
          getClass(),
          REST_CLIENT_UNEXPECTED_RESPONSE,
          "The request 'GET %s' returned a NULL body, "
              + "but was expecting a 200 status code of type '%s'",
          url,
          responseType.getSimpleName());
      return Optional.of(response.getBody());
    } catch (HttpStatusCodeException e) {
      if (NOT_FOUND.equals(e.getStatusCode())) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public boolean isFound(@NonNull String url) {
    try {
      // Any non-error response status code will result in True.
      get(url, Object.class);
      return true;
    } catch (HttpStatusCodeException e) {
      // Any notfound error status code will return false, otherwise propagate the error
      if (NOT_FOUND.equals(e.getStatusCode())) {
        return false;
      }
      throw e;
    }
  }

  public <T> ResponseEntity<T> get(String url, Class<T> responseType) {
    return retry.execute(
        retryContext -> {
          val httpEntity = new HttpEntity<>(new HttpHeaders());
          return rest.exchange(url, HttpMethod.GET, httpEntity, responseType);
        });
  }
}
