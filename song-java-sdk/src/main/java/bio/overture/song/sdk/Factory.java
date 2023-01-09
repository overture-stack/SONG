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

package bio.overture.song.sdk;

import bio.overture.song.core.retry.DefaultRetryListener;
import bio.overture.song.core.retry.RetryPolicies;
import bio.overture.song.sdk.config.RestClientConfig;
import bio.overture.song.sdk.config.RetryConfig;
import bio.overture.song.sdk.errors.ServerResponseErrorHandler;
import bio.overture.song.sdk.util.DefaultClientHttpRequestInterceptor;
import bio.overture.song.sdk.web.Endpoint;
import bio.overture.song.sdk.web.RestClient;
import bio.overture.song.sdk.web.impl.DefaultRestClient;
import bio.overture.song.sdk.web.impl.DefaultRetryRestClientDecorator;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Builder
@RequiredArgsConstructor
public class Factory {

  @NonNull private final RestClientConfig restClientConfig;
  @NonNull private final RetryConfig retryConfig;

  public SongApi buildSongApi() {
    return new SongApi(buildRestClient(restClientConfig, retryConfig), new Endpoint());
  }

  public static RestClient buildRestClient(@NonNull RestClientConfig restClientConfig) {
    return DefaultRestClient.builder().restTemplate(buildRestTemplate(restClientConfig)).build();
  }

  public static RestClient buildRestClient(
      @NonNull RestClientConfig restClientConfig, @NonNull RetryConfig retryConfig) {
    return DefaultRetryRestClientDecorator.builder()
        .restClient(buildRestClient(restClientConfig))
        .retryTemplate(buildRetryTemplate(retryConfig))
        .build();
  }

  public static RestTemplate buildRestTemplate(@NonNull RestClientConfig restClientConfig) {
    val r = new RestTemplate();
    r.setErrorHandler(new ServerResponseErrorHandler());
    r.setUriTemplateHandler(new DefaultUriBuilderFactory(restClientConfig.getServerUrl()));
    r.setRequestFactory(new SimpleClientHttpRequestFactory());
    r.getInterceptors()
        .add(new DefaultClientHttpRequestInterceptor(restClientConfig.getAccessToken()));
    return r;
  }

  private static RetryTemplate buildRetryTemplate(RetryConfig retryConfig) {
    val result = new RetryTemplate();
    result.setBackOffPolicy(defineBackOffPolicy(retryConfig));
    result.setRetryPolicy(
        new SimpleRetryPolicy(
            retryConfig.getMaxRetries(), RetryPolicies.getRetryableExceptions(), true));
    result.registerListener(new DefaultRetryListener(false));
    return result;
  }

  private static BackOffPolicy defineBackOffPolicy(RetryConfig config) {
    val backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(config.getInitialBackoff());
    backOffPolicy.setMultiplier(config.getMultiplier());
    return backOffPolicy;
  }
}
