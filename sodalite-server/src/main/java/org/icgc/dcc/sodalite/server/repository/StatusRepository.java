package org.icgc.dcc.sodalite.server.repository;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.SubmissionStatus;
import org.icgc.dcc.sodalite.server.repository.mapper.StatusMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(StatusMapper.class)
public interface StatusRepository {

  @SqlUpdate("INSERT INTO submissions (id, study_id, state, payload, updated_at) VALUES (:id, :studyId, :state, :payload, now())")
  int create(@Bind("id") String id, @Bind("studyId") String studyId, @Bind("state") String state,
      @Bind("payload") String jsonPayload);

  // note: avoiding handling datetime's in application; keeping it all in the SQL (also, see schema)
  @SqlUpdate("UPDATE submissions SET state = :state, errors = :errors, updated_at = now() WHERE id = :id AND study_id = :studyId")
  int update(@Bind("id") String id, @Bind("studyId") String studyId, @Bind("state") String state,
      @Bind("errors") String errors);

  @SqlQuery("SELECT id, study_id, state, created_at, updated_at, errors, payload FROM submissions WHERE id = :uploadId AND study_id = :studyId")
  SubmissionStatus get(@Bind("uploadId") String id, @Bind("studyId") String studyId);

  @SqlQuery("SELECT id FROM submissions WHERE id = :uploadId AND study_id = :studyId")
  List<String> checkIfExists(@Bind("uploadId") String id, @Bind("studyId") String studyId);

  @SqlUpdate("UPDATE submissions SET state=:newState WHERE state=:oldState AND study_id = :studyId")
  int updateState(@Bind("studyId") String studyId, @Bind("oldState") String oldState,
      @Bind("newState") String newState);

}
