package org.icgc.dcc.sodalite.server.service;

public class InvalidRepositoryDataException extends RuntimeException {

  public InvalidRepositoryDataException() {
    super();
  }

  public InvalidRepositoryDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public InvalidRepositoryDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidRepositoryDataException(String message) {
    super(message);
  }

  public InvalidRepositoryDataException(Throwable cause) {
    super(cause);
  }

}
