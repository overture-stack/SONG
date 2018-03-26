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

import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.repository.mapper.UploadMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

//TODO: [DCC-5643] Cleanup SQLQueries to reference constants
@RegisterMapper(UploadMapper.class)
public interface UploadRepository {

  @SqlUpdate("INSERT INTO upload (id, study_id, analysis_id, state, payload, updated_at) " +
          "VALUES (:id, :studyId, :analysisId, :state, :payload, now())")
  int create(@Bind("id") String id, @Bind("studyId") String studyId, @Bind("analysisId") String analysisId,
             @Bind("state") String state, @Bind("payload") String jsonPayload);

  @SqlQuery("SELECT id from upload where study_id=:studyId AND analysis_id=:analysisId")
  List<String> findByBusinessKey(@Bind("studyId") String studyId,
      @Bind("analysisId") String analysisId);

  @SqlUpdate("UPDATE upload set payload=:payload, state=:state, updated_at = now() WHERE id=:id")
  int update_payload(@Bind("id") String id, @Bind("state") String state, @Bind("payload") String payload);

  // note: avoiding handling datetime's in application; keeping it all in the SQL (also, see schema)
  @SqlUpdate("UPDATE upload SET state = :state, errors = :errors, updated_at = now() WHERE id = :id")
  int update(@Bind("id") String id, @Bind("state") String state, @Bind("errors") String errors);

  @SqlQuery("SELECT id, analysis_id, study_id, state, created_at, updated_at, errors, payload FROM upload WHERE id = :uploadId")
  Upload get(@Bind("uploadId") String id);

}
