package org.icgc.dcc.song.server.repository.exceptions;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ServiceException extends ServerException {

  private final Services service;


  public ServiceException(@NonNull Services service, ServerError serverError, String message, String debugMessage, String url) {
    super(serverError, message, debugMessage, url);
    this.service = service;
  }

  public ServiceException(@NonNull Services service, ServerError serverError, Throwable cause, String debugMessage,
      String url, String message) {
    super(serverError, message, cause, debugMessage, url);
    this.service = service;
  }

  public ServiceException(@NonNull Services service, ServerError serverError, String debugMessage, String message) {
    super(serverError, message, debugMessage);
    this.service = service;
  }

  public ServiceException(@NonNull Services service, ServerError serverError, String message) {
    super(serverError, message);
    this.service = service;
  }

  public ServiceException(@NonNull Services service, ServerError serverError, Throwable cause, String debugMessage,
      String message) {
    super(serverError, message, cause, debugMessage);
    this.service = service;
  }

  public ServiceException(@NonNull Services service, ServerError serverError, Throwable cause, String message) {
    super(serverError, message, cause);
    this.service = service;
  }
}
