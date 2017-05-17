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

  // id, study_id, state, errors, payload, analysis_object, created_by, created_at, updated_by, updated_at
  @SqlUpdate("INSERT INTO submissions (id, study_id, state, payload, created_by, created_at) VALUES (LOWER(:id), UPPER(:studyId), :state, :payload, :createdBy, now())")
  int create(@Bind("id") String id, @Bind("studyId") String studyId, @Bind("state") String state,
      @Bind("payload") String jsonPayload, @Bind("createdBy") String accessToken);

  // note: avoiding handling datetime's in application; keeping it all in the SQL (also, see schema)
  @SqlUpdate("UPDATE submissions SET state = :state, errors = :errors, updated_by = :updatedBy, updated_at = now() WHERE id = LOWER(:id) AND study_id = UPPER(:studyId)")
  int updateState(@Bind("id") String id, @Bind("studyId") String studyId, @Bind("state") String state,
      @Bind("errors") String errors, @Bind("updatedBy") String accessToken);

  @SqlUpdate("UPDATE submissions SET state = :state, analysis_object = :analysis_object, updated_by = :updatedBy, updated_at = now() WHERE id = LOWER(:id) AND study_id = UPPER(:studyId)")
  int updateAnalysis(@Bind("id") String id, @Bind("studyId") String studyId, @Bind("state") String state,
      @Bind("errors") String errors, @Bind("updatedBy") String accessToken);
  
  @SqlQuery("SELECT id, study_id, state, created_at, updated_at, errors, payload FROM submissions WHERE id = LOWER(:uploadId) AND study_id = UPPER(:studyId)")
  SubmissionStatus get(@Bind("uploadId") String id, @Bind("studyId") String studyId);

  @SqlQuery("SELECT id FROM submissions WHERE study_id = UPPER(:studyId) AND state = :state")
  List<String> getByState(@Bind("studyId") String studyId, @Bind("state") String state);

  @SqlQuery("SELECT id FROM submissions WHERE id = UPPER(:uploadId) AND study_id = UPPER(:studyId)")
  List<String> checkIfExists(@Bind("uploadId") String id, @Bind("studyId") String studyId);

}
