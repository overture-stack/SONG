package org.icgc.dcc.sodalite.server.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.icgc.dcc.sodalite.server.model.AnalysisState;
import org.icgc.dcc.sodalite.server.model.LibraryStrategy;
import org.icgc.dcc.sodalite.server.model.SequencingRead;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class SequencingReadMapper implements ResultSetMapper<SequencingRead> {
  
  public SequencingRead map(int index, ResultSet r, StatementContext ctx) throws SQLException { // I prefer braces on next
                                                                                                // line when declaring
                                                                                                // exception throws in method
                                                                                                // signature - Dušan
    {
      return new SequencingRead()
        .withAnalysisId(r.getString("id")) 
        .withAnalysisSubmitterId(r.getString("submitter_id"))
        .withStudyId(r.getString("study_id"))
        .withState(AnalysisState.fromValue(r.getString("state")))
        .withLibraryStrategy(LibraryStrategy.fromValue(r.getString("library_strategy")))
        .withPairedEnd(r.getBoolean("paired_end"))
        .withInsertSize(r.getInt("insert_size"))
        .withAligned(r.getBoolean("aligned"))
        .withAlignmentTool(r.getString("alignment_tool"))
        .withReferenceGenome(r.getString("reference_genome"));
    }
  }
}
