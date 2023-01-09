/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

package bio.overture.song.server.utils.generator;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LegacyAnalysisTypeName {
  VARIANT_CALL("variantCall"),
  SEQUENCING_READ("sequencingRead");

  @NonNull private final String analysisTypeName;

  public static LegacyAnalysisTypeName resolveLegacyAnalysisTypeName(
      @NonNull String analysisTypeName) {
    return stream(values())
        .filter(x -> x.getAnalysisTypeName().equals(analysisTypeName))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    format("The analysis type name '%s' cannot be resolved", analysisTypeName)));
  }
}
