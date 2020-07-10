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

package bio.overture.song.server.kafka;

import static bio.overture.song.core.utils.Checkers.checkNotBlank;
import static lombok.AccessLevel.PRIVATE;

import bio.overture.song.core.model.enums.AnalysisStates;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
//Note: although the AllArgs and NoArgs combination below seems odd,
// it allows Jackson to deserialize to an immutable object without using any additional annotations.
@AllArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
public class AnalysisMessage {

  @NonNull private final String analysisId;
  @NonNull private final String studyId;
  @NonNull private final String state;
  @NonNull private final String songServerId;

  public static AnalysisMessage createAnalysisMessage(
      String analysisId, String studyId, AnalysisStates analysisStates, String songServerId) {
    checkNotBlank(analysisId);
    return new AnalysisMessage(analysisId, studyId, analysisStates.toString(), songServerId);
  }
}
