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
package bio.overture.song.client.command;

import static bio.overture.song.sdk.model.SortDirection.DESC;
import static com.google.common.collect.Lists.newArrayList;

import bio.overture.song.client.util.EnumConverter;
import bio.overture.song.sdk.SongApi;
import bio.overture.song.sdk.model.ListAnalysisTypesRequest;
import bio.overture.song.sdk.model.SortDirection;
import bio.overture.song.sdk.model.SortOrder;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Retrieve schema information")
public class ListAnalysisTypesCommand extends Command {

  private static final String N_SWITCH = "-n";
  private static final String NAMES_SWITCH = "--names";
  private static final String VERSIONS_SWITCH = "--versions";
  private static final String V_SWITCH = "-v";
  private static final String UNRENDERED_ONLY_SWITCH = "--unrendered-only";
  private static final String U_SWITCH = "-u";
  private static final String HIDE_SCHEMA_SWITCH = "--hide-schema";
  private static final String HS_SWITCH = "-hs";
  private static final String SO_SWITCH = "-so";
  private static final String SORT_ORDER_SWITCH = "--sort-order";
  private static final String SD_SWITCH = "-sd";
  private static final String SORT_DIRECTION_SWITCH = "--sort-direction";
  private static final String O_SWITCH = "-o";
  private static final String OFFSET_SWITCH = "--offset";
  private static final String L_SWITCH = "-l";
  private static final String LIMIT_SWITCH = "--limit";

  @Parameter(
      names = {N_SWITCH, NAMES_SWITCH},
      description = "Filter analysisTypes by names",
      required = false,
      variableArity = true)
  private List<String> names = newArrayList();

  @Parameter(
      names = {V_SWITCH, VERSIONS_SWITCH},
      description = "Filter analysisTypes by versions",
      required = false,
      variableArity = true)
  private List<Integer> versions = newArrayList();

  @Parameter(
      names = {HS_SWITCH, HIDE_SCHEMA_SWITCH},
      description = "Hide the schema. Default is false",
      required = false)
  private boolean hideSchema = false;

  @Parameter(
      names = {U_SWITCH, UNRENDERED_ONLY_SWITCH},
      description =
          "Only retrieve the unrenedered schema that was initially registered. Default is false",
      required = false)
  private boolean unrenderedOnly = false;

  @Parameter(
      names = {SO_SWITCH, SORT_ORDER_SWITCH},
      description = "AnalysisType fields to sort on",
      converter = SortOrderConverter.class,
      required = false,
      variableArity = true)
  private List<SortOrder> sortOrders = newArrayList();

  @Parameter(
      names = {SD_SWITCH, SORT_DIRECTION_SWITCH},
      description = "Sorting direction. Default is DESC",
      converter = SortDirectionConverter.class,
      required = false)
  private SortDirection sortDirection = DESC;

  @Parameter(
      names = {O_SWITCH, OFFSET_SWITCH},
      description = "Query offset",
      required = false)
  private Integer offset;

  @Parameter(
      names = {L_SWITCH, LIMIT_SWITCH},
      description = "Query limit",
      required = false)
  private Integer limit;

  @NonNull private SongApi songApi;

  @Override
  public void run() throws IOException {
    val r =
        ListAnalysisTypesRequest.builder()
            .names(names)
            .versions(versions)
            .hideSchema(hideSchema)
            .unrenderedOnly(unrenderedOnly)
            .limit(limit)
            .offset(offset)
            .sortDirection(sortDirection)
            .sortOrders(sortOrders)
            .build();
    val response = songApi.listAnalysisTypes(r).getResultSet();
    prettyOutput(response);
  }

  public static class SortDirectionConverter extends EnumConverter<SortDirection> {
    @Override
    protected Class<SortDirection> getEnumClass() {
      return SortDirection.class;
    }
  }

  public static class SortOrderConverter extends EnumConverter<SortOrder> {
    @Override
    protected Class<SortOrder> getEnumClass() {
      return SortOrder.class;
    }
  }
}
