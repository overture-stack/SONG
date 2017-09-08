package org.icgc.dcc.song.client.errors;

public class IllegalCommandLineArgumentException extends RuntimeException {

  public IllegalCommandLineArgumentException() {
    super();
  }

  public IllegalCommandLineArgumentException(String message) {
    super(message);
  }

  public IllegalCommandLineArgumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalCommandLineArgumentException(Throwable cause) {
    super(cause);
  }

}
