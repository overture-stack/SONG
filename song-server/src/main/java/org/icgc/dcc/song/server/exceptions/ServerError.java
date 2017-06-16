package org.icgc.dcc.song.server.exceptions;

import org.springframework.http.HttpStatus;

public interface ServerError {

  String getErrorId();

  HttpStatus getHttpStatus();

}
