package org.icgc.dcc.song.server.repository.exceptions;

import org.springframework.http.HttpStatus;

public interface ServerError {

  String getId();

  HttpStatus getHttpStatus();

}
