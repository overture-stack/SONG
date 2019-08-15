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

import static com.google.common.collect.Lists.newArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Data;
import lombok.val;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoSearchRequest {

  @ApiModelProperty(notes = "If true, include the info field in the response, otherwise exclude it")
  private boolean includeInfo;

  private List<SearchTerm> searchTerms;

  @JsonIgnore
  public boolean hasSearchTerms() {
    return searchTerms.size() > 0;
  }

  public static InfoSearchRequest createInfoSearchRequest(
      boolean includeInfo, SearchTerm... searchTerms) {
    return createInfoSearchRequest(includeInfo, newArrayList(searchTerms));
  }

  public static InfoSearchRequest createInfoSearchRequest(
      boolean includeInfo, List<SearchTerm> searchTerms) {
    val r = new InfoSearchRequest();
    r.setSearchTerms(searchTerms);
    r.setIncludeInfo(includeInfo);
    return r;
  }
}
