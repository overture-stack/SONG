package bio.overture.song.server.utils.web;

import bio.overture.song.core.exceptions.ServerError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Value
public class ResponseOption {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @NonNull private final ResponseEntity<String> response;

  public <R> R extractOneEntity(@NonNull Class<R> entityClass) {
    return assertOk()
        .assertHasBody()
        .map(x -> internalExtractOneEntityFromResponse(x, entityClass));
  }

  public <R> ListAssert<R> assertPageResultsOfType(Class<R> entityClass) {
    return assertThat(extractPageResults(entityClass));
  }

  public <R> ObjectAssert<R> assertEntityOfType(Class<R> entityClass) {
    return assertThat(extractOneEntity(entityClass));
  }

  public <R> List<R> extractPageResults(@NonNull Class<R> entityClass) {
    return assertOk()
        .assertHasBody()
        .map(x -> internalExtractPageResultSetFromResponse(x, entityClass));
  }

  public <R> Set<R> extractManyEntities(@NonNull Class<R> entityClass) {
    return assertOk()
        .assertHasBody()
        .map(x -> internalExtractManyEntitiesFromResponse(x, entityClass));
  }

  public ResponseOption assertStatusCode(ServerError serverError) {
    return assertStatusCode(serverError.getHttpStatus());
  }

  public ResponseOption assertStatusCode(HttpStatus code) {
    AssertionsForClassTypes.assertThat(response.getStatusCode()).isEqualTo(code);
    return this;
  }

  public ResponseOption assertOk() {
    return assertStatusCode(OK);
  }

  public ResponseOption assertNotFound() {
    return assertStatusCode(NOT_FOUND);
  }

  public ResponseOption assertConflict() {
    return assertStatusCode(CONFLICT);
  }

  public ResponseOption assertBadRequest() {
    return assertStatusCode(BAD_REQUEST);
  }

  public ResponseOption assertHasBody() {
    AssertionsForClassTypes.assertThat(response.hasBody()).isTrue();
    AssertionsForClassTypes.assertThat(response.getBody()).isNotNull();
    return this;
  }

  public <R> R map(Function<ResponseEntity<String>, R> transformingFunction) {
    return transformingFunction.apply(getResponse());
  }

  @SneakyThrows
  private static <T> List<T> internalExtractPageResultSetFromResponse(
      ResponseEntity<String> r, Class<T> tClass) {
    val page = MAPPER.readTree(r.getBody());
    assertThat(page).isNotNull();
    return Streams.stream(page.path("content").iterator())
        .map(x -> MAPPER.convertValue(x, tClass))
        .collect(toImmutableList());
  }

  @SneakyThrows
  private static <T> T internalExtractOneEntityFromResponse(
      ResponseEntity<String> r, Class<T> tClass) {
    return MAPPER.readValue(r.getBody(), tClass);
  }

  @SneakyThrows
  private static <T> Set<T> internalExtractManyEntitiesFromResponse(
      ResponseEntity<String> r, Class<T> tClass) {
    return Streams.stream(MAPPER.readTree(r.getBody()).iterator())
        .map(x -> MAPPER.convertValue(x, tClass))
        .collect(toImmutableSet());
  }

}
