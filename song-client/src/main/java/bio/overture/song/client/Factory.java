package bio.overture.song.client;

import bio.overture.song.client.cli.ClientMain;
import bio.overture.song.client.config.ClientConfig;
import bio.overture.song.client.config.Config;
import bio.overture.song.client.config.RetryConfig;
import bio.overture.song.client.errors.ServerResponseErrorHandler;
import bio.overture.song.client.register.Registry;
import bio.overture.song.client.register.RestClient;
import bio.overture.song.client.util.DefaultClientHttpRequestInterceptor;
import bio.overture.song.core.retry.DefaultRetryListener;
import bio.overture.song.core.retry.RetryPolicies;
import lombok.NonNull;
import lombok.val;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

public class Factory {

  private final ClientConfig clientConfig;
  private final RetryConfig retryConfig;

  public Factory(@NonNull Config config) {
    this.clientConfig = config.getClient();
    this.retryConfig = config.getRetry();
    System.getProperties().putAll(config.getOtherProperties());
  }

  public ClientMain buildClientMain() {
    return new ClientMain(clientConfig, buildRegistry());
  }

  private Registry buildRegistry() {
    return new Registry(clientConfig, buildRestClient());
  }

  private RestClient buildRestClient() {
    return new RestClient(buildRestTemplate(), buildRetryTemplate());
  }

  private RestTemplate buildRestTemplate() {
    val r = new RestTemplate();
    r.setErrorHandler(new ServerResponseErrorHandler());
    r.setUriTemplateHandler(new DefaultUriBuilderFactory(clientConfig.getServerUrl()));
    r.setRequestFactory(new SimpleClientHttpRequestFactory());
    r.getInterceptors().add(new DefaultClientHttpRequestInterceptor(clientConfig.getAccessToken()));
    return r;
  }

  private RetryTemplate buildRetryTemplate() {
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
