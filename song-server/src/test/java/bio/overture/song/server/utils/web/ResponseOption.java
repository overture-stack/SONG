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

package bio.overture.song.server.utils.web;

import static bio.overture.song.core.exceptions.SongError.parseErrorResponse;
import static bio.overture.song.core.utils.Deserialization.deserializeList;
import static bio.overture.song.core.utils.Deserialization.deserializePage;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import bio.overture.song.core.exceptions.ServerError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Value
public class ResponseOption {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());

  @NonNull private final ResponseEntity<String> response;

  public <R> R extractOneEntity(@NonNull Class<R> entityClass) {
    return assertOk()
        .assertHasBody()
        .map(x -> internalExtractOneEntityFromResponse(x, entityClass));
  }

  public <R> ResponseOption assertOneEntityEquals(R expectedValue) {
    assertEquals(extractOneEntity(expectedValue.getClass()), expectedValue);
    return this;
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

  public ResponseOption assertIsError() {
    assertTrue(
        format(
            "Was expecting an error (4xx or 5xx status code), " + "however status code was [%s]",
            response.getStatusCode()),
        response.getStatusCode().isError());
    return this;
  }

  public ResponseOption assertServerError(ServerError serverError) {
    val songError = parseErrorResponse(response);
    assertEquals(serverError.getErrorId(), songError.getErrorId());
    return assertStatusCode(serverError.getHttpStatus());
  }

  public ResponseOption assertStatusCode(HttpStatus code) {
    assertEquals(code, response.getStatusCode());
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
    assertTrue(response.hasBody());
    assertNotNull(response.getBody());
    return this;
  }

  public <R> R map(Function<ResponseEntity<String>, R> transformingFunction) {
    return transformingFunction.apply(getResponse());
  }

  @SneakyThrows
  private static <T> List<T> internalExtractPageResultSetFromResponse(
      ResponseEntity<String> r, Class<T> tClass) {
    val page = deserializePage(r.getBody(), tClass);
    assertNotNull(page);
    return page.getResultSet();
  }

  @SneakyThrows
  private static <T> T internalExtractOneEntityFromResponse(
      ResponseEntity<String> r, Class<T> tClass) {
    return MAPPER.readValue(r.getBody(), tClass);
  }

  @SneakyThrows
  private static <T> Set<T> internalExtractManyEntitiesFromResponse(
      ResponseEntity<String> r, Class<T> tClass) {
    val contents = deserializeList(r.getBody(), tClass);
    return Set.copyOf(contents);
  }
}
