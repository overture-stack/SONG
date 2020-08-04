package bio.overture.song.server.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class DefaultPublicKeyFetcher implements PublicKeyFetcher {

  @NonNull private final String url;
  @NonNull private final RestTemplate restTemplate;
  @NonNull private final RetryTemplate retryTemplate;

  //  // TODO: [rtisma] add error handling
  @Override
  public String getPublicKey() {
    val resp = retryTemplate.execute(x -> restTemplate.getForEntity(url, String.class));
    return resp.hasBody() ? resp.getBody() : null;
  }
}
