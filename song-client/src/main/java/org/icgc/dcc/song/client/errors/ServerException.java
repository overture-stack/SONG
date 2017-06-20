package org.icgc.dcc.song.client.errors;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
public class ServerException extends RuntimeException {

  private final HttpStatus status;
  private final String id;
  private final String message;

}
