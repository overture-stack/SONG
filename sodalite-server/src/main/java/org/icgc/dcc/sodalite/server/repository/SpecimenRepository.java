package org.icgc.dcc.sodalite.server.repository;


import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.model.SpecimenClass;
import org.icgc.dcc.sodalite.server.model.SpecimenType;
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
		  @Bind("class") SpecimenClass class_, @Bind("type") SpecimenType type);

  @SqlQuery("SELECT id, s submitter_id, type FROM study WHERE study_id=:study_id and submitter_id=:submitter_id")
  List<Specimen> get(@Bind("type") String type);
  
  
}
