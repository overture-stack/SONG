package org.icgc.dcc.song.server.repository;

import com.marvinformatics.hibernate.json.JsonUserType;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomJsonType extends JsonUserType {
  public static final String CUSTOM_JSON_TYPE_PKG_PATH = "org.icgc.dcc.song.server.repository.CustomJsonType";

  public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws
      HibernateException, SQLException {
    return nullSafeGet(rs, names, (SessionImplementor) session, owner);
  }

  public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
    nullSafeSet(st, value, index, (SessionImplementor) session);
  }

}
