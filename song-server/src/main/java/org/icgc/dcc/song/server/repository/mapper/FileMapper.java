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

import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.icgc.dcc.song.server.model.enums.AccessTypes.resolveAccessType;
import static org.icgc.dcc.song.server.repository.AttributeNames.ACCESS;
import static org.icgc.dcc.song.server.repository.AttributeNames.ANALYSIS_ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.MD5;
import static org.icgc.dcc.song.server.repository.AttributeNames.NAME;
import static org.icgc.dcc.song.server.repository.AttributeNames.SIZE;
import static org.icgc.dcc.song.server.repository.AttributeNames.STUDY_ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.TYPE;


public class FileMapper implements ResultSetMapper<File> {

  @Override
  public File map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    return File.create(r.getString(ID), r.getString(ANALYSIS_ID), r.getString(NAME), r.getString(STUDY_ID),
        r.getLong(SIZE), r.getString(TYPE), r.getString(MD5), getFileAccess(r));
  }

  private static AccessTypes getFileAccess(ResultSet r) throws SQLException {
    return resolveAccessType(r.getString(ACCESS));
  }

}
