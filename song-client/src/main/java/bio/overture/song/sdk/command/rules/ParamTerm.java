/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.sdk.command.rules;

import static java.lang.String.format;

import java.util.function.Function;
import lombok.NonNull;
import lombok.Value;

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

  public static <T> ParamTerm<T> createParamTerm(
      String shortSwitch, String longSwitch, T value, Function<T, Boolean> isDefinedFunction) {
    return new ParamTerm<T>(shortSwitch, longSwitch, value, isDefinedFunction);
  }
}
