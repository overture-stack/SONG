package org.icgc.dcc.sodalite.server.repository;

import org.icgc.dcc.sodalite.server.model.Upload;
import org.icgc.dcc.sodalite.server.repository.mapper.UploadMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(UploadMapper.class)
public interface UploadRepository {

  @SqlUpdate("INSERT INTO upload (id, study_id, state, payload, updated_at) VALUES (:id, :studyId, :state, :payload, now())")
  int create(@Bind("id") String id, @Bind("studyId") String studyId, @Bind("state") String state,
      @Bind("payload") String jsonPayload);

  // note: avoiding handling datetime's in application; keeping it all in the SQL (also, see schema)
  @SqlUpdate("UPDATE upload SET state = :state, errors = :errors, updated_at = now() WHERE id = :id")
  int update(@Bind("id") String id, @Bind("state") String state, @Bind("errors") String errrors);

  @SqlQuery("SELECT id, study_id, state, created_at, updated_at, errors, payload FROM upload WHERE id = :uploadId")
  Upload get(@Bind("uploadId") String id);

}
