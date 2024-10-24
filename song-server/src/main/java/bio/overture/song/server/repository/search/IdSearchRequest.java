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

package bio.overture.song.server.repository.search;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IdSearchRequest {

  private static final String WILD_CARD = ".*";

  @ApiModelProperty(notes = "regex pattern. Default is wildcard")
  private final String objectId;

  public static IdSearchRequest createIdSearchRequest(String objectId) {
    return new IdSearchRequest(getGlobPattern(objectId));
  }

  public String getObjectId() {
    return getGlobPattern(this.objectId);
  }

  private static String getGlobPattern(String opt) {
    return opt == null ? WILD_CARD : opt;
  }
}
