package org.icgc.dcc.sodalite.server.repository;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.repository.mapper.SpecimenMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(SpecimenMapper.class)
public interface SpecimenRepository {

  @SqlUpdate("INSERT INTO specimen (id, study_id, donor_id, submitter_id, class, type) VALUES (:id, :study_id, :donor_id, :submitter_id, :class, :type)")
  int save(@Bind("id") String id, @Bind("study_id") String study_id, @Bind("donor_id") String donor_id, @Bind("submitter_id") String submitter_id,
      @Bind("class") String class_, @Bind("type") String type);

  @SqlUpdate("UPDATE specimen SET study_id=:study_id, donor_id=:donor_id, submitter_id=:submitter_id, class=:class, type=:type where id=:id")
  int update(@Bind("id") String id, @Bind("study_id") String study_id, @Bind("donor_id") String donor_id, @Bind("submitter_id") String submitter_id,
      @Bind("class") String class_, @Bind("type") String type);

  @SqlQuery("SELECT id, study_id, donor_id, submitter_id, class, type FROM specimen where donor_id=:donor_id")
  List<Specimen> findByParentId(@Bind("donor_id") String donor_id);

  @SqlQuery("SELECT id, study_id, donor_id, submitter_id, class, type FROM specimen where id=:id")
  Specimen getById(@Bind("id") String id);

  @SqlQuery("SELECT id, study_id, donor_id, submitter_id, class, type FROM sample WHERE study_id=:study_id AND submitter_id=:submitter_id")
  Specimen getByBusinessKey(@Bind("study_id") String studyId, @Bind("submitter_id") String submitter_id);
  
  @SqlQuery("SELECT id from specimen where donor_id=:donor_id")
  List<String> getIds(@Bind("donor_id") String donor_id);

  @SqlUpdate("DELETE from specimen where id=:id")
  int delete(@Bind("id") String id);
}
