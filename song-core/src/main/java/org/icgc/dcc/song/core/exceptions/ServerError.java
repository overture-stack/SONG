package org.icgc.dcc.song.core.exceptions;

import org.springframework.http.HttpStatus;

public interface ServerError {

  String getErrorId();

  HttpStatus getHttpStatus();

}
