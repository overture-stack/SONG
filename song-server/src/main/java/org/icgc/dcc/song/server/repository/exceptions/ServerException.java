package org.icgc.dcc.song.server.repository.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Getter
public class ServerException extends RuntimeException {

  private static String DEFAULT_URL = "noUrl";
  private static String DEFAULT_DEBUG_MESSAGE = "";

  @NonNull private final ServerError serverError;
  @NonNull private final String debugMessage;
  @NonNull private final String url;

  public ServerException(ServerError serverError, String message, String debugMessage, String url) {
    super(message);
    this.serverError = serverError;
    this.debugMessage = debugMessage;
    this.url = url;
  }

  public ServerException(ServerError serverError, String message, Throwable cause,
      String debugMessage, String url) {
    super(message, cause);
    this.serverError = serverError;
    this.debugMessage = debugMessage;
    this.url = url;
  }

  public ServerException(ServerError serverError, String message, String debugMessage) {
    this(serverError, message, debugMessage, DEFAULT_URL);
  }

  public ServerException(ServerError serverError, String message) {
    this(serverError, message, DEFAULT_DEBUG_MESSAGE, DEFAULT_URL);
  }

  public ServerException(ServerError serverError, String message, Throwable cause, String debugMessage) {
    this(serverError, message, cause, debugMessage,  DEFAULT_URL);
  }

  public ServerException(ServerError serverError, String message, Throwable cause) {
    this(serverError, message, cause, DEFAULT_DEBUG_MESSAGE,  DEFAULT_URL);
  }

  public Error getError(){
    val error = new Error();
    error.setDebugMessage(getDebugMessage());
    error.setHttpStatus(getServerError().getHttpStatus());
    error.setId(getServerError().getId());
    error.setMessage(getMessage());
    error.setStackTrace(getStackTrace());
    return error;
  }


}
