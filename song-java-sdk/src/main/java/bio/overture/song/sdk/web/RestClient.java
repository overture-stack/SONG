package bio.overture.song.sdk.web;

import bio.overture.song.core.exceptions.ServerException;
import java.util.List;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;

public interface RestClient {

  <R> ResponseEntity<R> get(String endpoint, Class<R> responseType) throws ServerException;

  <R> ResponseEntity<R> post(String endpoint, Object body, Class<R> responseType)
      throws ServerException;

  <R> ResponseEntity<R> put(String endpoint, Object body, Class<R> responseType)
      throws ServerException;

  <R> ResponseEntity<List<R>> putList(String endpoint, Object body, Class<R> responseType)
      throws ServerException;

  <R> ResponseEntity<List<R>> getList(String endpoint, Class<R> responseType)
      throws ServerException;

  <R> ResponseEntity<List<R>> postList(String endpoint, Object body, Class<R> responseType)
      throws ServerException;

  default <R> ResponseEntity<List<R>> postList(String endpoint, Class<R> responseType)
      throws ServerException {
    return postList(endpoint, null, responseType);
  }

  default <R> ResponseEntity<List<R>> putList(String endpoint, Class<R> responseType)
      throws ServerException {
    return putList(endpoint, null, responseType);
  }

  default <R> ResponseEntity<R> put(@NonNull String endpoint, @NonNull Class<R> responseType)
      throws ServerException {
    return put(endpoint, null, responseType);
  }

  default <R> ResponseEntity<R> post(@NonNull String endpoint, @NonNull Class<R> responseType)
      throws ServerException {
    return post(endpoint, null, responseType);
  }
}
