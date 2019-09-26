package bio.overture.song.sdk.web;

import static bio.overture.song.core.utils.JsonUtils.mapper;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.ResponseEntity.status;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.model.PageDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.http.ResponseEntity;

public interface RestClient {

  ObjectMapper MAPPER = mapper();

  <R> ResponseEntity<R> get(String endpoint, Class<R> responseType) throws ServerException;

  <R> ResponseEntity<R> post(String endpoint, Object body, Class<R> responseType)
      throws ServerException;

  <R> ResponseEntity<R> put(String endpoint, Object body, Class<R> responseType)
      throws ServerException;

  default <R> ResponseEntity<List<R>> putList(String endpoint, Object body, Class<R> contentType)
      throws ServerException {
    return convertToList(put(endpoint, body, String.class), contentType);
  }

  default <R> ResponseEntity<List<R>> getList(String endpoint, Class<R> contentType)
      throws ServerException {
    return convertToList(get(endpoint, String.class), contentType);
  }

  default <R> ResponseEntity<List<R>> postList(String endpoint, Object body, Class<R> contentType)
      throws ServerException {
    return convertToList(post(endpoint, body, String.class), contentType);
  }

  default <R> ResponseEntity<PageDTO<R>> getPage(String endpoint, Class<R> contentType)
      throws ServerException {
    return convertToPage(get(endpoint, String.class), contentType);
  }

  default <R> ResponseEntity<PageDTO<R>> putPage(String endpoint, Object body, Class<R> contentType)
      throws ServerException {
    return convertToPage(put(endpoint, body, String.class), contentType);
  }

  default <R> ResponseEntity<PageDTO<R>> postPage(
      String endpoint, Object body, Class<R> contentType) throws ServerException {
    return convertToPage(post(endpoint, body, String.class), contentType);
  }

  default <R> ResponseEntity<PageDTO<R>> postPage(String endpoint, Class<R> contentType)
      throws ServerException {
    return postPage(endpoint, null, contentType);
  }

  default <R> ResponseEntity<List<R>> postList(String endpoint, Class<R> responseType)
      throws ServerException {
    return postList(endpoint, null, responseType);
  }

  default <R> ResponseEntity<List<R>> putList(String endpoint, Class<R> responseType)
      throws ServerException {
    return putList(endpoint, null, responseType);
  }

  default <R> ResponseEntity<PageDTO<R>> putPage(String endpoint, Class<R> contentType)
      throws ServerException {
    return putPage(endpoint, null, contentType);
  }

  default <R> ResponseEntity<R> put(String endpoint, Class<R> responseType) throws ServerException {
    return put(endpoint, null, responseType);
  }

  default <R> ResponseEntity<R> post(String endpoint, Class<R> responseType)
      throws ServerException {
    return post(endpoint, null, responseType);
  }

  @SneakyThrows
  default <T> ResponseEntity<PageDTO<T>> convertToPage(
      @NonNull ResponseEntity<String> response, @NonNull Class<T> contentType) {
    val reader = MAPPER.readerFor(new TypeReference<PageDTO<T>>() {});
    val erasedPage = reader.<PageDTO<T>>readValue(response.getBody());
    val contents =
        erasedPage.getResultSet().stream()
            .map(x -> MAPPER.convertValue(x, contentType))
            .collect(toUnmodifiableList());
    val pageDTO = new PageDTO<T>();
    pageDTO.setOffset(erasedPage.getOffset());
    pageDTO.setLimit(erasedPage.getLimit());
    pageDTO.setCount(erasedPage.getCount());
    pageDTO.setResultSet(contents);
    return status(response.getStatusCode()).body(pageDTO);
  }

  @SneakyThrows
  default <T> ResponseEntity<List<T>> convertToList(
      @NonNull ResponseEntity<String> response, @NonNull Class<T> contentType) {
    val reader = MAPPER.readerFor(new TypeReference<List<T>>() {});
    val erasedList = reader.<List<T>>readValue(response.getBody());
    val contents =
        erasedList.stream()
            .map(x -> MAPPER.convertValue(x, contentType))
            .collect(toUnmodifiableList());
    return status(response.getStatusCode()).body(contents);
  }
}
