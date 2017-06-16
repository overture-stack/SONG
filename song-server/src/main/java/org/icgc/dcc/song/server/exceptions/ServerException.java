package org.icgc.dcc.song.server.exceptions;

import lombok.Getter;
import lombok.val;

@Getter
public class ServerException extends RuntimeException {

  private static String DEFAULT_URL = "noUrl";
  private static String DEFAULT_DEBUG_MESSAGE = "";

  private final ServerError serverError;
  private final String debugMessage;
  private final String url;
  private final long timestamp;


  public ServerException(ServerError serverError, String message, String debugMessage, String url) {
    super(message);
    this.serverError = serverError;
    this.debugMessage = debugMessage;
    this.url = url;
    this.timestamp = System.currentTimeMillis();
  }

  public ServerException(ServerError serverError, String message, Throwable cause,
      String debugMessage, String url) {
    super(message, cause);
    this.serverError = serverError;
    this.debugMessage = debugMessage;
    this.url = url;
    this.timestamp = System.currentTimeMillis();
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
    error.setErrorId(getServerError().getErrorId());
    error.setMessage(getMessage());
    error.setStackTrace(getStackTrace());
    error.setTimestamp(timestamp);
    return error;
  }


}
