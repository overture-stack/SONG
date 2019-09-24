package bio.overture.song.sdk.factory;

import bio.overture.song.core.retry.DefaultRetryListener;
import bio.overture.song.core.retry.RetryPolicies;
import bio.overture.song.sdk.config.RestClientConfig;
import bio.overture.song.sdk.config.RetryConfig;
import bio.overture.song.client.errors.ServerResponseErrorHandler;
import bio.overture.song.sdk.register.Endpoint;
import bio.overture.song.sdk.register.Registry;
import bio.overture.song.sdk.rest.RestClient;
import bio.overture.song.sdk.rest.impl.SpringRestClient;
import bio.overture.song.sdk.rest.impl.SpringRetryRestClientDecorator;
import bio.overture.song.client.util.DefaultClientHttpRequestInterceptor;
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
public class SpringRegistryFactory extends AbstractRegistryFactory{

  @NonNull private final RestClientConfig restClientConfig;
  @NonNull private final RetryConfig retryConfig;

  @Override
  public Registry build() {
    return new Registry(buildFinalRestClient(), new Endpoint());
  }

  private RestClient buildFinalRestClient() {
    return SpringRetryRestClientDecorator.builder()
        .restClient(buildSpringRestClient(restClientConfig))
        .retryTemplate(buildRetryTemplate(retryConfig))
        .build();
  }

  private static SpringRestClient buildSpringRestClient(RestClientConfig restClientConfig){
    return SpringRestClient.builder()
        .restTemplate(buildRestTemplate(restClientConfig))
        .build();
  }

  private static RestTemplate buildRestTemplate(RestClientConfig restClientConfig) {
    val r = new RestTemplate();
    r.setErrorHandler(new ServerResponseErrorHandler());
    r.setUriTemplateHandler(new DefaultUriBuilderFactory(restClientConfig.getServerUrl()));
    r.setRequestFactory(new SimpleClientHttpRequestFactory());
    r.getInterceptors().add(new DefaultClientHttpRequestInterceptor(restClientConfig.getAccessToken()));
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
