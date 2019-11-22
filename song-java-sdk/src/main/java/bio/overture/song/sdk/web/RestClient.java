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

package bio.overture.song.sdk.web;

import static bio.overture.song.core.utils.Deserialization.deserializeList;
import static bio.overture.song.core.utils.Deserialization.deserializePage;
import static org.springframework.http.ResponseEntity.status;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.model.PageDTO;
import java.util.List;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.http.ResponseEntity;

public interface RestClient {

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
    val pageDTO = deserializePage(response.getBody(), contentType);
    return status(response.getStatusCode()).body(pageDTO);
  }

  @SneakyThrows
  default <T> ResponseEntity<List<T>> convertToList(
      @NonNull ResponseEntity<String> response, @NonNull Class<T> contentType) {
    val contents = deserializeList(response.getBody(), contentType);
    return status(response.getStatusCode()).body(contents);
  }
}
