package org.icgc.dcc.sodalite.server.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class ExistsMapper implements ResultSetMapper<Boolean> {

  public Boolean map(int index, ResultSet rs, StatementContext ctx) throws SQLException {
      return rs.first();
  }
}