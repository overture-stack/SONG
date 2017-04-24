package org.icgc.dcc.sodalite.server.repository.mapper;

import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.model.SpecimenType;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SpecimenMapper implements ResultSetMapper<Specimen> {

  public Specimen map(int index, ResultSet r, StatementContext ctx) throws SQLException
  { // I prefer braces on next line when declaring exception throws in method signature - Dušan
    return Specimen.builder()
        .id(r.getString("id"))
        .donor_id(r.getString("donor_id"))
        .submitter_id(r.getString("submitter_id"))
        .type(SpecimenType.valueOf(r.getString("type")))
        .build();
  }
}
