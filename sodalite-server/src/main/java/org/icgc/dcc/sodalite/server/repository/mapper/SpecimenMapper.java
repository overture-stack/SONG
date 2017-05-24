package org.icgc.dcc.sodalite.server.repository.mapper;

import org.icgc.dcc.sodalite.server.model.entity.Specimen;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SpecimenMapper implements ResultSetMapper<Specimen> {

  public Specimen map(int index, ResultSet r, StatementContext ctx) throws SQLException { // I prefer braces on next
                                                                                          // line when declaring
                                                                                          // exception throws in method
                                                                                          // signature - Dušan
    return new Specimen()
        .withSpecimenId(r.getString("id"))
        .withSpecimenClass(Specimen.SpecimenClass.fromValue(r.getString("class")))
        .withSpecimenType(Specimen.SpecimenType.fromValue(r.getString("type")))
        .withSpecimenSubmitterId(r.getString("submitter_id"));
  }
}
