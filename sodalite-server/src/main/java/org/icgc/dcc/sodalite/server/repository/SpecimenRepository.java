package org.icgc.dcc.sodalite.server.repository;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Specimen;
import org.icgc.dcc.sodalite.server.repository.mapper.SpecimenMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(SpecimenMapper.class)
public interface SpecimenRepository {

  @SqlUpdate("INSERT INTO Specimen (id, submitter_id, donor_id, class, type) VALUES (:specimenId, :specimenSubmitterId, :donorId, :specimenClass, :specimenType)")
  int create(@BindBean Specimen specimen);

  @SqlQuery("SELECT id, submitter_id, donor_id, class, type FROM Specimen where id=:id")
  Specimen read(@Bind("id") String id);

  @SqlUpdate("UPDATE Specimen SET submitter_id=:specimenSubmitterId, class=:specimenClass, type=:specimenType where id=:specimenId")
  int update(@BindBean Specimen specimen);

  @SqlUpdate("DELETE from Specimen where id=:id")
  int delete(@Bind("id") String id);

  @SqlQuery("SELECT id, submitter_id, donor_id, class, type FROM Specimen where donor_id=:donor_id")
  List<Specimen> readByParentId(@Bind("donor_id") String donor_id);

  @SqlQuery("SELECT id from Specimen where donor_id=:donor_id")
  List<String> findByParentId(@Bind("donor_id") String donor_id);

  @SqlQuery("SELECT s.id from Specimen s, Donor d "
      + "WHERE s.submitter_id=:submitterId "
      + "AND s.donor_id = d.id "
      + "AND d.study_id=:studyId")
  String findByBusinessKey(@Bind("studyId") String studyId, @Bind("submitterId") String submitterId);
}
