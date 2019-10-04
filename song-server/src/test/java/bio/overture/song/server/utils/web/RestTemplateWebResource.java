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
