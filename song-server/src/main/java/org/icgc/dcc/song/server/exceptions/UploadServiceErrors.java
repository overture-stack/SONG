package org.icgc.dcc.song.server.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Getter
@RequiredArgsConstructor
public enum UploadServiceErrors implements ServerError {

  UPLOAD_REPOSITORY_CREATE_RECORD("upload.repository.create.record", INTERNAL_SERVER_ERROR),
  PAYLOAD_PARSING("payload.parsing", UNPROCESSABLE_ENTITY);

  @NonNull private final String errorId;
  @NonNull private final HttpStatus httpStatus;

}
