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

import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.enums.UploadStates;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;
import static org.icgc.dcc.song.server.repository.AttributeNames.ANALYSIS_ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.CREATED_AT;
import static org.icgc.dcc.song.server.repository.AttributeNames.ERRORS;
import static org.icgc.dcc.song.server.repository.AttributeNames.ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.PAYLOAD;
import static org.icgc.dcc.song.server.repository.AttributeNames.STATE;
import static org.icgc.dcc.song.server.repository.AttributeNames.STUDY_ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.UPDATED_AT;

public class UploadMapper implements ResultSetMapper<Upload> {

  @Override
  public Upload map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
    return Upload.create(rs.getString(ID),
        rs.getString(STUDY_ID),
        rs.getString(ANALYSIS_ID),
        getState(rs),
        rs.getString(ERRORS),
        rs.getString(PAYLOAD),
        rs.getTimestamp(CREATED_AT).toLocalDateTime(),
        rs.getTimestamp(UPDATED_AT).toLocalDateTime());
  }

  private static UploadStates getState(ResultSet r) throws SQLException {
    return resolveState(r.getString(STATE));
  }



}
