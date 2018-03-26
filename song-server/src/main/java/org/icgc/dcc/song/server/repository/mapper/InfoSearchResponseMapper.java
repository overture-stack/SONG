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
package org.icgc.dcc.song.server.repository.mapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.icgc.dcc.song.server.repository.search.InfoSearchResponse;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.icgc.dcc.song.core.utils.JsonUtils.readTree;
import static org.icgc.dcc.song.server.model.enums.InfoSearchResponseColumns.ANALYSIS_ID;
import static org.icgc.dcc.song.server.model.enums.InfoSearchResponseColumns.INFO;
import static org.icgc.dcc.song.server.repository.search.InfoSearchResponse.createWithInfo;
import static org.icgc.dcc.song.server.repository.search.InfoSearchResponse.createWithoutInfo;

@RequiredArgsConstructor
public class InfoSearchResponseMapper implements ResultSetMapper<InfoSearchResponse> {

  private final boolean includeInfo;

  @Override
  @SneakyThrows
  public InfoSearchResponse map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    if (includeInfo){
      return createWithInfo(r.getString(ANALYSIS_ID.toString()), readTree(r.getString(INFO.toString())));
    } else {
      return createWithoutInfo(r.getString(ANALYSIS_ID.toString()));
    }
  }

  public static InfoSearchResponseMapper createInfoSearchResponseMapper(boolean includeInfo) {
    return new InfoSearchResponseMapper(includeInfo);
  }

}
