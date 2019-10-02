package bio.overture.song.client.util;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import lombok.NonNull;
import lombok.val;

import java.util.EnumSet;

import static java.lang.String.format;
import static bio.overture.song.core.utils.Joiners.COMMA;

public abstract class EnumConverter<E extends Enum<E>> implements IStringConverter<E> {

  protected abstract Class<E> getEnumClass();

  @Override
  public E convert(@NonNull String value) {
    val enumSet = EnumSet.allOf(getEnumClass());
    return enumSet.stream()
        .filter(x -> x.toString().equals(value))
        .findFirst()
        .orElseThrow(
            () ->
                new ParameterException(
                    format(
                        "Value '%s' cannot be converted to %s. Available values are: [%s]",
                        value, getEnumClass().getSimpleName(), COMMA.join(enumSet))));
  }
}
