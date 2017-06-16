package org.icgc.dcc.song.server.exceptions;

import org.springframework.http.HttpStatus;

public interface ServerError {

  String getId();

  HttpStatus getHttpStatus();

}
