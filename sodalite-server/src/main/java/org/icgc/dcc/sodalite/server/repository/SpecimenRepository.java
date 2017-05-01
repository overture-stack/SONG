package org.icgc.dcc.sodalite.server.repository;


import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.repository.mapper.SpecimenMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(SpecimenMapper.class)
public interface SpecimenRepository {
  @SqlUpdate("INSERT INTO Specimen (id, donor_id, submitter_id, class, type) VALUES (:id, :donor_id, :submitter_id, :class, :type)")
  int save(@Bind("id") String id, @Bind("donor_id") String donor_id, @Bind("submitter_id") String submitter_id, 
		  @Bind("class") String class_, @Bind("type") String type);

  @SqlQuery("SELECT id,submitter_id, class, type FROM Specimen where id=:id")
  List<Specimen> getById(@Bind("id") String id);
}
