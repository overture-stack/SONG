package org.icgc.dcc.sodalite.server.repository;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.File;
import org.icgc.dcc.sodalite.server.repository.mapper.FileMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(FileMapper.class)
public interface FileRepository {

  @SqlUpdate("INSERT INTO File (id,      name,      sample_id, size,      type,      md5,      metadata_doc) "
      + "VALUES (:objectId, :fileName, :sampleId, :fileSize, :fileType, :fileMd5, :metadata)")
  int create(@BindBean File f);

  @SqlQuery("SELECT id, name, sample_id, size, type, md5, metadata_doc FROM File WHERE id=:id")
  File read(@Bind("id") String id);

  @SqlUpdate("UPDATE File SET name=:fileName, size=:fileSize, type=:fileType, md5=:fileMd5, metadata_doc=:metadata where id=:objectId")
  int update(@BindBean File file);

  @SqlUpdate("DELETE From File where id=:id")
  int delete(@Bind("id") String id);

  @SqlQuery("SELECT id, name, sample_id, size, type, md5, metadata_doc FROM File WHERE sample_id=:sampleId")
  List<File> readByParentId(@Bind("sampleId") String sample_id);

  @SqlQuery("SELECT f.id from File f, Sample s, Specimen sp, Donor d "
      + "WHERE f.name=:fileName "
      + "AND f.sample_id = s.id "
      + "AND s.specimen_id = sp.id "
      + "AND sp.donor_id = d.id "
      + "AND d.study_id = :studyId")
  String findByBusinessKey(@Bind("studyId") String studyId, @Bind("fileName") String fileName);
}
