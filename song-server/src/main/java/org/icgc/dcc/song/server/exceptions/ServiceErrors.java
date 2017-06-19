package org.icgc.dcc.song.server.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Getter
@RequiredArgsConstructor
public enum ServiceErrors implements ServerError {

  UPLOAD_REPOSITORY_CREATE_RECORD("upload.repository.create.record", UNPROCESSABLE_ENTITY),
  GENERATOR_CLOCK_MOVED_BACKWARDS("generator.clock.moved.backwards", INTERNAL_SERVER_ERROR),
  PAYLOAD_PARSING("payload.parsing", UNPROCESSABLE_ENTITY),
  UNKNOWN_ERROR("unknown.error", INTERNAL_SERVER_ERROR);

  @NonNull private final String errorId;
  @NonNull private final HttpStatus httpStatus;

}
