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

package bio.overture.song.core.exceptions;

import static com.google.common.base.Preconditions.checkArgument;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import lombok.NonNull;
import org.springframework.http.HttpStatus;

public enum ServerErrors implements ServerError {
  STUDY_ID_DOES_NOT_EXIST(NOT_FOUND),
  UPLOAD_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  ANALYSIS_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  SEQUENCING_READ_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  VARIANT_CALL_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  INFO_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  INFO_REPOSITORY_UPDATE_RECORD(UNPROCESSABLE_ENTITY),
  INFO_REPOSITORY_DELETE_RECORD(UNPROCESSABLE_ENTITY),
  FILE_REPOSITORY_UPDATE_RECORD(UNPROCESSABLE_ENTITY),
  STUDY_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  FILE_REPOSITORY_DELETE_RECORD(UNPROCESSABLE_ENTITY),
  GENERATOR_CLOCK_MOVED_BACKWARDS(INTERNAL_SERVER_ERROR),
  PAYLOAD_PARSING(UNPROCESSABLE_ENTITY),
  UPLOAD_ID_NOT_FOUND(NOT_FOUND),
  FILE_NOT_FOUND(NOT_FOUND),
  LEGACY_ENTITY_NOT_FOUND(NOT_FOUND),
  SUPPRESSED_STATE_TRANSITION(BAD_REQUEST),
  INFO_NOT_FOUND(NOT_FOUND),
  INVALID_FILE_UPDATE_REQUEST(BAD_REQUEST),
  ILLEGAL_FILE_UPDATE_REQUEST(BAD_REQUEST),
  UPLOAD_ID_NOT_VALIDATED(CONFLICT),
  ANALYSIS_ID_NOT_CREATED(INTERNAL_SERVER_ERROR),
  ANALYSIS_MISSING_FILES(INTERNAL_SERVER_ERROR),
  ANALYSIS_ID_NOT_FOUND(NOT_FOUND),
  SEQUENCING_READ_NOT_FOUND(NOT_FOUND),
  VARIANT_CALL_NOT_FOUND(NOT_FOUND),
  UNAUTHORIZED_TOKEN(UNAUTHORIZED),
  FORBIDDEN_TOKEN(FORBIDDEN),
  GATEWAY_IS_DOWN(GATEWAY_TIMEOUT),
  GATEWAY_TIMED_OUT(GATEWAY_TIMEOUT),
  BAD_REPLY_FROM_GATEWAY(BAD_GATEWAY),
  GATEWAY_SERVICE_NOT_FOUND(BAD_GATEWAY),
  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
  NOT_IMPLEMENTED_YET(NOT_IMPLEMENTED),
  FILE_REPOSITORY_CREATE_RECORD(INTERNAL_SERVER_ERROR),
  ANALYSIS_STATE_UPDATE_FAILED(INTERNAL_SERVER_ERROR),
  SEARCH_TERM_SYNTAX(BAD_REQUEST),
  ANALYSIS_TYPE_INCORRECT_VERSION(CONFLICT),
  ANALYSIS_ID_COLLISION(CONFLICT),
  INFO_ALREADY_EXISTS(CONFLICT),
  VARIANT_CALL_CORRUPTED_DUPLICATE(INTERNAL_SERVER_ERROR),
  SEQUENCING_READ_CORRUPTED_DUPLICATE(INTERNAL_SERVER_ERROR),
  STUDY_ALREADY_EXISTS(CONFLICT),
  MISMATCHING_STORAGE_OBJECT_SIZES(CONFLICT),
  MISMATCHING_STORAGE_OBJECT_CHECKSUMS(CONFLICT),
  MISSING_STORAGE_OBJECTS(CONFLICT),
  ILLEGAL_FILTER_PARAMETER(BAD_REQUEST),
  ILLEGAL_QUERY_PARAMETER(BAD_REQUEST),
  UNREGISTERED_TYPE(INTERNAL_SERVER_ERROR),
  ANALYSIS_TYPE_ALREADY_EXISTS(CONFLICT),
  ANALYSIS_TYPE_NOT_FOUND(NOT_FOUND),
  ENTITY_NOT_RELATED_TO_STUDY(BAD_REQUEST),
  MALFORMED_PARAMETER(BAD_REQUEST),
  INVALID_STORAGE_DOWNLOAD_RESPONSE(INTERNAL_SERVER_ERROR),
  STUDY_ID_MISMATCH(CONFLICT),
  STUDY_ID_MISSING(BAD_REQUEST),
  SCHEMA_VIOLATION(BAD_REQUEST),
  ID_NOT_FOUND(NOT_FOUND),
  REST_CLIENT_UNEXPECTED_RESPONSE(BAD_GATEWAY),
  ID_SERVICE_ERROR(BAD_GATEWAY),
  MALFORMED_JSON_SCHEMA(BAD_REQUEST),
  STORAGE_SERVICE_ERROR(BAD_GATEWAY),
  STORAGE_OBJECT_NOT_FOUND(
      NOT_FOUND), // Used STORAGE instead of SCORE to not tie it to a specific implementation
  UNKNOWN_ERROR(INTERNAL_SERVER_ERROR),
  ILLEGAL_ANALYSIS_TYPE_NAME(BAD_REQUEST),
  DUPLICATE_ANALYSIS_SCHEMA(INTERNAL_SERVER_ERROR),
  DUPLICATE_ANALYSIS_DATA(INTERNAL_SERVER_ERROR);

  private static final String ERROR_ID_SEPARATOR = ".";
  private static final String ENUM_NAME_SEPARATOR = "_";
  private static final String REGEX = "[A-Z0-9_]+";

  private final String errorId;
  private final HttpStatus httpStatus;

  ServerErrors(@NonNull HttpStatus httpStatus) {
    this.httpStatus = httpStatus;
    this.errorId = extractErrorId(this.name());
  }

  public String getErrorId() {
    return errorId;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public static String extractErrorId(@NonNull String errorId) {
    checkArgument(
        errorId.matches(REGEX), "The errorId [%s] must follow the regex: %s", errorId, REGEX);
    return errorId.toLowerCase().replaceAll(ENUM_NAME_SEPARATOR, ERROR_ID_SEPARATOR);
  }
}
