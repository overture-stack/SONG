package bio.overture.song.core.exceptions;

import static java.lang.String.format;

public class BooleanConversionException extends RuntimeException {

  public BooleanConversionException() {}

  public BooleanConversionException(String formattedMessage, Object... args) {
    super(format(formattedMessage, args));
  }

  public BooleanConversionException(Throwable cause, String formattedMessage, Object... args) {
    super(format(formattedMessage, args), cause);
  }

  public BooleanConversionException(Throwable cause) {
    super(cause);
  }

  public BooleanConversionException(
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace,
      String formattedMessage,
      Object... args) {
    super(format(formattedMessage, args), cause, enableSuppression, writableStackTrace);
  }
}
