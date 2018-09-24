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

package bio.overture.song.server.model.enums;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@RequiredArgsConstructor
public enum AnalysisTypes {

  SEQUENCING_READ(Constants.SEQUENCING_READ_TYPE),
  VARIANT_CALL(Constants.VARIANT_CALL_TYPE);

  private final String text;

  public String toString(){
    return text;
  }

  public static AnalysisTypes resolveAnalysisType(@NonNull String analysisType){
    return stream(values())
        .filter(x -> x.toString().equals(analysisType))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(format("The analysis type '%s' cannot be resolved", analysisType)));
  }

}
