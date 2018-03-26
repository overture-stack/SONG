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
import lombok.val;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;

import static java.lang.String.format;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.resolveAnalysisType;
import static org.icgc.dcc.song.server.repository.AttributeNames.ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.STATE;
import static org.icgc.dcc.song.server.repository.AttributeNames.STUDY_ID;
import static org.icgc.dcc.song.server.repository.AttributeNames.TYPE;

public class AnalysisMapper implements ResultSetMapper<Analysis> {

  @Override
  @SneakyThrows
  public Analysis map(int index, ResultSet r, StatementContext ctx) {
    val id = r.getString(ID);
    val study = r.getString(STUDY_ID );
    val type = r.getString(TYPE);
    val state = r.getString(STATE);

    val analysisType = resolveAnalysisType(type);
    if (analysisType == SEQUENCING_READ) {
      return SequencingReadAnalysis.create(id, study, state);
    }else if (analysisType == VARIANT_CALL) {
      return VariantCallAnalysis.create(id, study, state);
    }
    throw new IllegalStateException(
        format("Cannot process the analysisType '%s(%s)'", analysisType.name(), analysisType));
  }


}
