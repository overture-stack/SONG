/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.icgc.dcc.song.server.repository;

import java.util.List;

import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.repository.mapper.SpecimenMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(SpecimenMapper.class)
public interface SpecimenRepository {

  @SqlUpdate("INSERT INTO Specimen (id, submitter_id, donor_id, class, type) VALUES (:specimenId, " +
          ":specimenSubmitterId, :donorId, :specimenClass, :specimenType)")
  int create(@BindBean Specimen specimen);

  @SqlQuery("SELECT id, submitter_id, donor_id, class, type FROM Specimen where id=:id")
  Specimen read(@Bind("id") String id);

  @SqlUpdate("UPDATE Specimen SET submitter_id=:specimenSubmitterId, class=:specimenClass, type=:specimenType " +
          "where id=:specimenId")
  int update(@BindBean Specimen specimen);

  @SqlUpdate("UPDATE Specimen SET submitter_id=:specimenSubmitterId, class=:specimenClass, type=:specimenType " +
          "where id=:id")
  int update(@Bind("id") String id, @BindBean Specimen specimen);

  @SqlUpdate("DELETE from Specimen where id=:id")
  int delete(@Bind("id") String id);

  @SqlQuery("SELECT id from Specimen where donor_id=:donor_id")
  List<String> findByParentId(@Bind("donor_id") String donor_id);

  @SqlQuery("SELECT s.id from Specimen s, Donor d "
      + "WHERE s.submitter_id=:submitterId "
      + "AND s.donor_id = d.id "
      + "AND d.study_id=:studyId")
  String findByBusinessKey(@Bind("studyId") String studyId, @Bind("submitterId") String submitterId);
}
