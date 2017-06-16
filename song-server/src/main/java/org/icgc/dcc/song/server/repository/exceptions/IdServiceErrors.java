package org.icgc.dcc.song.server.repository.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor public enum IdServiceErrors implements ServerError {
  GENERATOR_CLOCK_MOVED_BACKWARDS("generator.clock.moved.backwards", HttpStatus.CONFLICT);

  @NonNull private final String id;
  @NonNull private final HttpStatus httpStatus;

}
