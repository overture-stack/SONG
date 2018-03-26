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

import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.repository.mapper.StudyMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(StudyMapper.class)
public interface StudyRepository {

  @SqlUpdate("INSERT INTO study (id, name,  organization, description) VALUES (:id, :name, :organization, :description)")
  int create(@Bind("id") String id, @Bind("name") String name, @Bind("organization") String organization,
      @Bind("description") String description);

  @SqlUpdate("UPDATE study SET name=:name, description=:description where id=:id")
  int set(@Bind("id") String id, @Bind("name") String name, @Bind("description") String description);

  @SqlQuery("SELECT id, name, organization, description FROM Study WHERE id = :studyId")
  Study read(@Bind("studyId") String id);

  @SqlQuery("SELECT id from Study")
  List<String> findAllStudies();
}
