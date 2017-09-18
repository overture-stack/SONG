package org.icgc.dcc.song.client.command.rules;

import lombok.NonNull;
import lombok.Value;

import java.util.function.Function;

import static java.lang.String.format;

@Value
public class ParamTerm<T> {

  @NonNull private final String shortSwitch;
  @NonNull private final String longSwitch;
  private final T value;
  @NonNull private final Function<T, Boolean> isDefinedFunction;

  public boolean isDefined() {
    return isDefinedFunction.apply(value);
  }

  public String getShortLongSymbol() {
    return format("'%s/%s'", shortSwitch, longSwitch);
  }

  public static <T> ParamTerm<T> createParamTerm(String shortSwitch,
      String longSwitch, T value, Function<T, Boolean> isDefinedFunction) {
    return new ParamTerm<T>(shortSwitch, longSwitch, value, isDefinedFunction);
  }

}
