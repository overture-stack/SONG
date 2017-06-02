package org.icgc.dcc.sodalite.server.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.icgc.dcc.sodalite.server.model.entity.Specimen;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import lombok.val;

public class SpecimenMapper implements ResultSetMapper<Specimen> {

  @Override
  public Specimen map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    val metadata = ""; // TODO: Fix this once we modify the database tables in the next ticket
    return Specimen.create(r.getString("id"), r.getString("submitter_id"), r.getString("donor_id"),
        r.getString("class"), r.getString("type"), metadata);
  }

}
