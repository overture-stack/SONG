package org.icgc.dcc.song.core.exceptions;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

import static com.google.common.base.Preconditions.checkArgument;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Getter
public enum ServerErrors implements ServerError {

  UPLOAD_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  GENERATOR_CLOCK_MOVED_BACKWARDS(INTERNAL_SERVER_ERROR),
  PAYLOAD_PARSING(UNPROCESSABLE_ENTITY),
  UPLOAD_ID_NOT_FOUND(NOT_FOUND ),
  UPLOAD_ID_NOT_VALIDATED(CONFLICT),
  ANALYSIS_ID_NOT_CREATED(INTERNAL_SERVER_ERROR),
  UNAUTHORIZED_TOKEN(UNAUTHORIZED),
  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
  UNPUBLISHED_FILE_IDS(CONFLICT),
  NOT_IMPLEMENTED_YET(NOT_IMPLEMENTED),
  SAMPLE_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  SPECIMEN_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  DONOR_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  FILE_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  UNKNOWN_ERROR(INTERNAL_SERVER_ERROR);

  private static final Character ERROR_ID_SEPARATOR = '.';
  private static final String REGEX = "[A-Z0-9_]+";

  @NonNull private final String errorId;
  @NonNull private final HttpStatus httpStatus;

  ServerErrors(HttpStatus httpStatus){
    this.httpStatus = httpStatus;
    this.errorId = extractErrorId(this.name());
  }


  public static String extractErrorId(String errorId){
    checkArgument(errorId.matches(REGEX),
        "The errorId [%s] must follow the regex: %s", errorId, REGEX);
    return errorId.toLowerCase().replaceAll("_",".");
  }

}
