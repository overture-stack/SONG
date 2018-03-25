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

import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.mapper.FileMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(FileMapper.class)
public interface FileRepository {

  @SqlUpdate("INSERT INTO File (id, analysis_id, name, study_id, size, type, md5, access) "
      + "VALUES (:objectId, :analysisId,:fileName,  :studyId, :fileSize, :fileType, :fileMd5sum, :fileAccess)")
  int create(@BindBean File f);

  @SqlQuery("SELECT id, analysis_id, name, study_id, size, type, md5, access FROM File WHERE id=:id")
  File read(@Bind("id") String id);

  @SqlUpdate("UPDATE File SET name=:fileName, analysis_id=:analysisId, size=:fileSize, type=:fileType, " +
          "md5=:fileMd5sum, access=:fileAccess where id=:objectId")
  int update(@BindBean File file);

  @SqlUpdate("DELETE From File where id=:id")
  int delete(@Bind("id") String id);

  @SqlQuery("SELECT id, analysis_id, name, study_id, size, type, md5, access FROM File WHERE study_id=:studyId")
  List<File> readByParentId(@Bind("studyId") String study_id);

  @SqlQuery("SELECT id from File WHERE name=:fileName AND analysis_id=:analysisId")
  String findByBusinessKey(@Bind("analysisId") String analysisId, @Bind("fileName") String fileName);
}
