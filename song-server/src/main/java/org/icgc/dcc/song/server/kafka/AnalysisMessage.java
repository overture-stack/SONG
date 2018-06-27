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

package org.icgc.dcc.song.server.kafka;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.icgc.dcc.song.core.model.enums.AnalysisStates;

import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.song.core.utils.Checkers.checkNotBlank;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class AnalysisMessage {
  @NonNull private final String analysisId;
  @NonNull private final String state;

  public static AnalysisMessage createAnalysisMessage(String analysisId,
      AnalysisStates analysisStates ){
    checkNotBlank(analysisId);
    return new AnalysisMessage(analysisId, analysisStates.toString());
  }

}
