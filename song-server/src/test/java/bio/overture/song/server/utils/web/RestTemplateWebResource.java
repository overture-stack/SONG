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

package bio.overture.song.server.utils.web;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

public class RestTemplateWebResource extends AbstractWebResource<RestTemplateWebResource> {

  private final RestTemplate restTemplate;
  private final RetryTemplate retryTemplate;

  public RestTemplateWebResource(
      @NonNull String serverUrl,
      @NonNull RetryTemplate retryTemplate,
      @NonNull RestTemplate restTemplate) {
    super(serverUrl);
    this.restTemplate = restTemplate;
    this.retryTemplate = retryTemplate;
  }

  @Override
  public ResponseEntity<String> executeRequest(
      HttpMethod httpMethod, String endpoint, HttpHeaders headers, String body) {
    return retryTemplate.execute(
        r ->
            restTemplate.exchange(
                endpoint, httpMethod, new HttpEntity<>(body, headers), String.class));
  }

  @Value
  @Builder
  public static class Factory {
    @NonNull private final String serverUrl;
    @NonNull private final RetryTemplate retryTemplate;
    @NonNull private final RestTemplate restTemplate;

    public RestTemplateWebResource create() {
      return new RestTemplateWebResource(serverUrl, retryTemplate, restTemplate);
    }
  }
}
