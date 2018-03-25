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

import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.repository.mapper.DonorMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(DonorMapper.class)
public interface DonorRepository {

  @SqlUpdate("INSERT INTO Donor (id, submitter_id, study_id, gender) " +
             "VALUES (:donorId, :donorSubmitterId, :studyId, :donorGender)")
  int create(@BindBean Donor donor);

  @SqlQuery("SELECT id, submitter_id, study_id, gender FROM donor WHERE id=:id")
  Donor read(@Bind("id") String donorId);

  @SqlUpdate("UPDATE Donor SET submitter_id=:donorSubmitterId, gender=:donorGender WHERE id=:donorId")
  int update(@BindBean Donor donor);

  @SqlUpdate("UPDATE Donor SET submitter_id=:donorSubmitterId, gender=:donorGender WHERE id=:id")
  int update(@Bind("id") String id, @BindBean Donor donor);

  @SqlUpdate("DELETE from donor where id=:id AND study_id=:studyId")
  int delete(@Bind("studyId") String studyId, @Bind("id") String id);


  @SqlQuery("SELECT id from donor where study_id=:studyId")
  List<String> findByParentId(@Bind("studyId") String parentId);

  @SqlQuery("SELECT id from donor where study_id=:studyId AND submitter_id=:key")
  String findByBusinessKey(@Bind("studyId") String studyId, @Bind("key") String key);
}

