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
import org.icgc.dcc.song.server.model.experiment.VariantCall;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;

import static org.icgc.dcc.song.server.repository.AttributeNames.ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.MATCHED_NORMAL_SAMPLE_SUBMITTER_ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.VARIANT_CALLING_TOOL;

public class VariantCallMapper implements ResultSetMapper<VariantCall> {

  @Override
  @SneakyThrows
  public VariantCall map(int index, ResultSet r, StatementContext ctx) {
    return VariantCall.create(r.getString(ID),
                r.getString(VARIANT_CALLING_TOOL),
                r.getString(MATCHED_NORMAL_SAMPLE_SUBMITTER_ID));
  }

}
