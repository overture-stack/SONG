package org.icgc.dcc.sodalite.server.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.icgc.dcc.sodalite.server.model.entity.Specimen;
import org.icgc.dcc.sodalite.server.model.enums.SpecimenClass;
import org.icgc.dcc.sodalite.server.model.enums.SpecimenType;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class SpecimenMapper implements ResultSetMapper<Specimen> {

  @Override
  public Specimen map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    return new Specimen(r.getString("id"),
        r.getString("submitter_id"),
        r.getString("donor_id"),
        SpecimenClass.fromValue(r.getString("class")),
        SpecimenType.fromValue(r.getString("type")));
  }

}
