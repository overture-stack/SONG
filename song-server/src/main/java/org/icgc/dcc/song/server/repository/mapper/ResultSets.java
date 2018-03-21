package org.icgc.dcc.song.server.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSets {

  public static Boolean getWrappedBoolean(ResultSet r, String columnName ) throws SQLException {
    return r.getObject(columnName,Boolean.class);
  }

  public static Long getWrappedLong(ResultSet r, String columnName ) throws SQLException {
    return r.getObject(columnName, Long.class);
  }

}
