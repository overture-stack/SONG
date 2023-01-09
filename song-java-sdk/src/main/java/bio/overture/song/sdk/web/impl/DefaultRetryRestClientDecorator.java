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

package bio.overture.song.sdk.web.impl;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.sdk.web.RestClient;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;

@Value
@Builder
public class DefaultRetryRestClientDecorator implements RestClient {

  @NonNull private final RetryTemplate retryTemplate;
  @NonNull private final RestClient restClient;

  @Override
  public <R> ResponseEntity<R> get(String endpoint, Class<R> responseType) throws ServerException {
    return retryTemplate.execute(r -> restClient.get(endpoint, responseType));
  }

  @Override
  public <R> ResponseEntity<R> post(String endpoint, Object body, Class<R> responseType)
      throws ServerException {
    return retryTemplate.execute(r -> restClient.post(endpoint, body, responseType));
  }

  @Override
  public <R> ResponseEntity<R> put(String endpoint, Object body, Class<R> responseType)
      throws ServerException {
    return retryTemplate.execute(r -> restClient.put(endpoint, body, responseType));
  }
}
