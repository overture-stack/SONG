package org.icgc.dcc.sodalite.server.repository;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.repository.mapper.FileMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

@UseStringTemplate3StatementLocator
@RegisterMapper(FileMapper.class)
public interface FileRepository {

  @SqlUpdate("INSERT INTO file (id, study_id, sample_id, name, size, type, md5, metadata_doc) VALUES (:id, :study_id, :sample_id, :name, :size, :file_type, :file_md5, :metadata_doc)")
  int save(@Bind("id") String id, @Bind("study_id") String studyId, @Bind("sample_id") String sampleId,
      @Bind("name") String name,
      @Bind("size") Long size, @Bind("file_type") String fileType, @Bind("file_md5") String fileMd5,
      @Bind("metadata_doc") String metadataDoc);

  @SqlUpdate("UPDATE file SET study_id=:study_id, sample_id=:sample_id, name=:name, size=:size, type=:file_type, md5=:file_md5, metadata_doc=:metadata_doc where id=:id")
  int update(@Bind("id") String id, @Bind("study_id") String studyId, @Bind("sample_id") String sampleId,
      @Bind("name") String name, @Bind("size") Long size, @Bind("file_type") String fileType,
      @Bind("file_md5") String fileMd5, @Bind("metadata_doc") String metadataDoc);

  @SqlUpdate("DELETE From file where id=:id")
  int delete(@Bind("id") String id);

  @SqlUpdate("DELETE From file where sample_id=:sample_id")
  int deleteBySampleId(@Bind("sample_id") String sampleId);

  @SqlQuery("SELECT id, study_id, sample_id, name, size, type, md5, metadata_doc FROM file WHERE id=:id")
  File getById(@Bind("id") String id);

  @SqlQuery("SELECT id, study_id, sample_id, name, size, type, md5, metadata_doc FROM file WHERE study_id=:study_id AND name=:name")
  File getByBusinessKey(@Bind("study_id") String studyId, @Bind("name") String name);

  @SqlQuery("SELECT id, study_id, sample_id, name, size, type, md5, metadata_doc FROM file WHERE sample_id=:sample_id")
  List<File> findByParentId(@Bind("sample_id") String sampleId);

  @SqlQuery("SELECT id, study_id, sample_id, name, size, type, md5, metadata_doc FROM file WHERE id IN (\\<id_list\\>)")
  List<File> findByAssociation(@BindIn("id_list") List<String> idList);
}
