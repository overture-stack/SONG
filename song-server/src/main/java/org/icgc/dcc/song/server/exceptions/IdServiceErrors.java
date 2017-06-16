package org.icgc.dcc.song.server.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
@RequiredArgsConstructor
public enum IdServiceErrors implements ServerError {
  GENERATOR_CLOCK_MOVED_BACKWARDS("generator.clock.moved.backwards", INTERNAL_SERVER_ERROR);

  @NonNull private final String errorId;
  @NonNull private final HttpStatus httpStatus;

}
