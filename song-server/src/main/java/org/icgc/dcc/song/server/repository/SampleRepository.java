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

import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.repository.mapper.SampleMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(SampleMapper.class)
public interface SampleRepository {

  @SqlUpdate("INSERT INTO Sample (id, submitter_id, specimen_id, type) " +
             "VALUES (:sampleId, :sampleSubmitterId, :specimenId, :sampleType)")
  int create(@BindBean Sample sample);

  @SqlQuery("SELECT id, submitter_id, specimen_id, type FROM Sample WHERE id=:id")
  Sample read(@Bind("id") String id);

  @SqlQuery("SELECT id, submitter_id, specimen_id, type FROM Sample WHERE specimen_id=:specimen_id")
  List<Sample> readByParentId(@Bind("specimen_id") String specimenId);

  @SqlUpdate("UPDATE Sample SET submitter_id=:sampleSubmitterId, type=:sampleType where id=:sampleId")
  int update(@BindBean Sample sample);

  @SqlUpdate("UPDATE Sample SET submitter_id=:sampleSubmitterId, type=:sampleType where id=:id")
  int update(@Bind("id") String id, @BindBean Sample sample);

  @SqlUpdate("DELETE from Sample where id=:id")
  int delete(@Bind("id") String id);

  @SqlUpdate("DELETE from Sample where specimen_id=:specimenId")
  String deleteByParentId(String specimenId);

  @SqlQuery("SELECT id from Sample where specimen_id=:specimenId")
  List<String> findByParentId(@Bind("specimenId") String specimen_id);

  @SqlQuery("SELECT s.id "
      + "FROM Sample s, Specimen sp, Donor d "
      + "WHERE s.submitter_id = :submitterId AND "
      + "s.specimen_id = sp.id AND "
      + "sp.donor_id = d.id AND "
      + "d.study_id = :studyId")
  String findByBusinessKey(@Bind("studyId") String studyId, @Bind("submitterId") String submitterId);
}