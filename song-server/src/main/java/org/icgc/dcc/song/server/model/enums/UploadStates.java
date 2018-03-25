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

package org.icgc.dcc.song.server.model.enums;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@RequiredArgsConstructor
public enum UploadStates {

  CREATED("CREATED"),
  VALIDATED("VALIDATED"),
  VALIDATION_ERROR("VALIDATION_ERROR"),
  UPLOADED("UPLOADED"),
  UPDATED("UPDATED"),
  SAVED("SAVED");

  @Getter
  private final String text;

  public static UploadStates resolveState(@NonNull String uploadState){
      return stream(values())
          .filter(x -> x.getText().equals(uploadState))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(format("The upload state '%s' cannot be resolved",
              uploadState)));
  }

}
