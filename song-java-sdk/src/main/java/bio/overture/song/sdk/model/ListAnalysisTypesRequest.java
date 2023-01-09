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

package bio.overture.song.sdk.model;

import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ListAnalysisTypesRequest {
  @NonNull private final List<String> names;
  @NonNull private final List<Integer> versions;
  @NonNull private final List<SortOrder> sortOrders;

  /** Nullable */
  private final Integer offset;

  private final Integer limit;
  private final Boolean hideSchema;
  private final Boolean unrenderedOnly;
  private final SortDirection sortDirection;
}
