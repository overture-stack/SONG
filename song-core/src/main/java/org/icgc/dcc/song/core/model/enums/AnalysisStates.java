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

package org.icgc.dcc.song.core.model.enums;

import lombok.NonNull;
import org.icgc.dcc.common.core.util.stream.Streams;

import static java.lang.String.format;
import static java.util.Arrays.stream;

public enum AnalysisStates {
  PUBLISHED,
  UNPUBLISHED,
  SUPPRESSED;

  public String toString(){
    return this.name();
  }

  public static AnalysisStates resolveAnalysisState(@NonNull String analysisState) {
    return Streams.stream(values())
        .filter(x -> x.toString().equals(analysisState))
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException(format("The analysis state '%s' cannot be resolved", analysisState)));
  }

  public static String[] toStringArray(){
    return stream(values())
        .map(Enum::name)
        .toArray(String[]::new);
  }

}
