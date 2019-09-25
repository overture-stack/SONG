package bio.overture.song.sdk.web.impl;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.sdk.web.RestClient;
import java.util.List;
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

  @Override
  public <R> ResponseEntity<List<R>> putList(String endpoint, Object body, Class<R> responseType)
      throws ServerException {
    return retryTemplate.execute(r -> restClient.putList(endpoint, body, responseType));
  }

  @Override
  public <R> ResponseEntity<List<R>> getList(String endpoint, Class<R> responseType)
      throws ServerException {
    return retryTemplate.execute(r -> restClient.getList(endpoint, responseType));
  }

  @Override
  public <R> ResponseEntity<List<R>> postList(String endpoint, Object body, Class<R> responseType)
      throws ServerException {
    return retryTemplate.execute(r -> restClient.postList(endpoint, body, responseType));
  }
}
