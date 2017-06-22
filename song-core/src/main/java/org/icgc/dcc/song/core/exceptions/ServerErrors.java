package org.icgc.dcc.song.core.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.springframework.http.HttpStatus;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.regex.Pattern.compile;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Getter
public enum ServerErrors implements ServerError {

  UPLOAD_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  GENERATOR_CLOCK_MOVED_BACKWARDS(INTERNAL_SERVER_ERROR),
  PAYLOAD_PARSING(UNPROCESSABLE_ENTITY),
  UPLOAD_ID_NOT_FOUND(NOT_FOUND ),
  UPLOAD_ID_NOT_VALIDATED(CONFLICT),
  ANALYSIS_ID_NOT_CREATED(INTERNAL_SERVER_ERROR),
  UNPUBLISHED_FILE_IDS(CONFLICT),
  NOT_IMPLEMENTED_YET(NOT_IMPLEMENTED),
  SAMPLE_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  DONOR_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  FILE_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  UNKNOWN_ERROR(INTERNAL_SERVER_ERROR);

  private static final Pattern PATTERN = compile("[A-Z0-9_]+");

  private String errorId;
  @NonNull private final HttpStatus httpStatus;

  ServerErrors(HttpStatus httpStatus){
    this.httpStatus = httpStatus;
    this.errorId = null;
  }

  public String getErrorId() {
    if (errorId == null){
      this.errorId = extractErrorId(name());
    }
    return errorId;
  }

  public static String extractErrorId(String errorId){
    val matcher = PATTERN.matcher(errorId);
    checkArgument(matcher.matches(),
        "The errorId [%s] must follow the regex: %s", errorId, PATTERN.pattern());
    return errorId.toLowerCase().replaceAll("_",".");
  }

}
