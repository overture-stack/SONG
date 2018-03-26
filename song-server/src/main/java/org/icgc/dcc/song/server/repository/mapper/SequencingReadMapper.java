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

import lombok.SneakyThrows;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.icgc.dcc.song.server.repository.AttributeNames.ALIGNED;
import static org.icgc.dcc.song.server.repository.AttributeNames.ALIGNMENT_TOOL;
import static org.icgc.dcc.song.server.repository.AttributeNames.ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.INSERT_SIZE;
import static org.icgc.dcc.song.server.repository.AttributeNames.LIBRARY_STRATEGY;
import static org.icgc.dcc.song.server.repository.AttributeNames.PAIRED_END;
import static org.icgc.dcc.song.server.repository.AttributeNames.REFERENCE_GENOME;
import static org.icgc.dcc.song.server.repository.mapper.ResultSets.getWrappedBoolean;
import static org.icgc.dcc.song.server.repository.mapper.ResultSets.getWrappedLong;

public class SequencingReadMapper implements ResultSetMapper<SequencingRead> {

  @Override
  @SneakyThrows
  public SequencingRead map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    return SequencingRead.create(
        r.getString(ID),
        getWrappedBoolean(r,ALIGNED),
        r.getString(ALIGNMENT_TOOL),
        getWrappedLong(r,INSERT_SIZE),
        r.getString(LIBRARY_STRATEGY),
        getWrappedBoolean(r, PAIRED_END),
        r.getString(REFERENCE_GENOME));
  }

}
