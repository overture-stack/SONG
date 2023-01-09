package bio.overture.song.core.utils;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

import bio.overture.song.core.exceptions.BooleanConversionException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class Booleans {

  public static boolean convertToBoolean(String value) throws BooleanConversionException {
    if (isNull(value)) {
      throw new BooleanConversionException("Cannot convert 'null' to boolean");
    } else if ("true".equalsIgnoreCase(value)) {
      return TRUE;
    } else if ("false".equalsIgnoreCase(value)) {
      return FALSE;
    } else {
      throw new BooleanConversionException(
          "The following value cannot be converted to a boolean: %s", value);
    }
  }
}
