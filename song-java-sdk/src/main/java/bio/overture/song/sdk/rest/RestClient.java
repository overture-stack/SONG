package bio.overture.song.sdk.rest;

import lombok.NonNull;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RestClient {

  <R> ResponseEntity<R> get(String endpoint, Class<R> responseType);
  <R> ResponseEntity<R> post(String endpoint, Object body, Class<R> responseType);
  <R> ResponseEntity<R> put(String endpoint, Object body, Class<R> responseType);

  <R> ResponseEntity<List<R>> putList(String endpoint, Object body, Class<R> responseType);
  <R> ResponseEntity<List<R>> getList(String endpoint, Class<R> responseType);
  <R> ResponseEntity<List<R>> postList(String endpoint, Object body, Class<R> responseType);


  default <R> ResponseEntity<R> put(@NonNull String endpoint, @NonNull Class<R> responseType){
    return put(endpoint, null, responseType);
  }

  default <R> ResponseEntity<R> post(@NonNull String endpoint, @NonNull Class<R> responseType){
    return post(endpoint, null, responseType);

  }

}
