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

package bio.overture.song.server.repository.search;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoSearchResponse {

  @NonNull private final String analysisId;
  private final JsonNode info;

  @JsonIgnore
  public boolean hasInfo() {
    return !isNull(info);
  }

  public static InfoSearchResponse createWithoutInfo(String analysisId) {
    return createWithInfo(analysisId, null);
  }

  public static InfoSearchResponse createWithInfo(String analysisId, JsonNode info) {
    return new InfoSearchResponse(analysisId, info);
  }
}
