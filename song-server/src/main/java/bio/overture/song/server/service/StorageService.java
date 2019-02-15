/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
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

package bio.overture.song.server.service;

import bio.overture.song.server.model.StorageObject;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

import static bio.overture.song.core.exceptions.ServerErrors.INVALID_STORAGE_DOWNLOAD_RESPONSE;
import static bio.overture.song.core.exceptions.ServerErrors.STORAGE_OBJECT_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static java.lang.Boolean.parseBoolean;
import static org.icgc.dcc.common.core.util.Joiners.SLASH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;

@Slf4j
@RequiredArgsConstructor
@Builder
public class StorageService {

  private static final String UPLOAD = "upload";
  private static final String DOWNLOAD= "download";
  private static final String OBJECT_MD5 = "objectMd5";
  private static final String OBJECT_SIZE= "objectSize";

  /**
   * Dependencies
   */
  @NonNull private final RestTemplate restTemplate;
  @NonNull private final RetryTemplate retryTemplate;
  @NonNull private final String storageUrl;
  @NonNull private final ValidationService validationService;
  @NonNull private final String scoreAuthorizationHeader;

  @SneakyThrows
  public boolean isObjectExist(@NonNull String accessToken, @NonNull String objectId) {
    return doGetBoolean(accessToken, getObjectExistsUrl(objectId));
  }

  @SneakyThrows
  public StorageObject downloadObject(@NonNull String objectId){
    val objectExists = isObjectExist(scoreAuthorizationHeader, objectId);
    checkServer(objectExists,getClass(), STORAGE_OBJECT_NOT_FOUND,
        "The object with objectId '%s' does not exist in the storage server", objectId);
    return convertStorageDownloadResponse(objectId, getStorageDownloadResponse(scoreAuthorizationHeader, objectId));
  }

  private JsonNode getStorageDownloadResponse(String accessToken, String objectId){
    val objectSpecification = doGetJson(accessToken, getDownloadObjectUrl(objectId));
    val validationError = validationService.validateStorageDownloadResponse(objectSpecification);
    if (validationError.isPresent()){
      throw buildServerException(getClass(),
          INVALID_STORAGE_DOWNLOAD_RESPONSE,
          "The validation of the storage download response for objectId '%s' failed with the following errors: %s",
          objectId, validationError.get());
    }
    return objectSpecification;
  }

  private StorageObject convertStorageDownloadResponse(String objectId, JsonNode storageDownloadResponse){
    return StorageObject.builder()
        .fileMd5sum(parseObjectMd5(storageDownloadResponse))
        .fileSize(parseObjectSize(storageDownloadResponse))
        .objectId(objectId)
        .build();
  }

  @SneakyThrows
  private String doGetString(String accessToken, String urlString){
    val url = new URL(urlString);
    ResponseEntity<String> response = retryTemplate.execute(retryContext -> {
      val httpHeaders = new HttpHeaders();
      httpHeaders.set(AUTHORIZATION, accessToken);
      val req = new HttpEntity<>(httpHeaders);
      return restTemplate.exchange(url.toURI(), GET, req, String.class);
    });
    return response.getBody();
  }

  private Boolean doGetBoolean(String accessToken, String url){
    return parseBoolean(doGetString(accessToken, url));
  }

  @SneakyThrows
  private JsonNode doGetJson(String accessToken, String url){
    return readTree(doGetString(accessToken, url));
  }

  private static String parseObjectMd5(JsonNode objectSpecification){
    return objectSpecification.path(OBJECT_MD5).textValue();
  }

  private static Long parseObjectSize(JsonNode objectSpecification){
    return objectSpecification.path(OBJECT_SIZE).asLong();
  }

  private String getObjectExistsUrl(String objectId){
    return joinUrl(storageUrl, UPLOAD, objectId);
  }

  private String getDownloadObjectUrl(String objectId){
    return joinUrl(storageUrl, DOWNLOAD, objectId)+"?offset=0&length=-1";
  }

  private static String joinUrl(String ... path){
    return SLASH.join(path);
  }

}
