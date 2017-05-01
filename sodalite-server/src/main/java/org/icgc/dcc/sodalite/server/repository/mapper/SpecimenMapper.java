package org.icgc.dcc.sodalite.server.repository.mapper;

import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.model.SpecimenClass;
import org.icgc.dcc.sodalite.server.model.SpecimenType;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class SpecimenMapper implements ResultSetMapper<Specimen> {

  public Specimen map(int index, ResultSet r, StatementContext ctx) throws SQLException
  { // I prefer braces on next line when declaring exception throws in method signature - Dušan
	if (r == null) {
		log.error("Result set was null in Specimen mapper");
	}
	log.info("Got id='" + r.getString("id"));
	log.info("Got submitter_id='" + r.getString("submitter_id") + "'");
	log.info("Got type '" + r.getString("type") + "'");
	log.info("Got class='" + r.getString("class") + "'");
    return new Specimen()
    	.withSpecimenId(r.getString("id"))
    	.withSpecimenClass(SpecimenClass.fromValue(r.getString("class")))
    	.withSpecimenType(SpecimenType.fromValue(r.getString("type")))
    	.withSpecimenSubmitterId(r.getString("submitter_id"));
  }
}
