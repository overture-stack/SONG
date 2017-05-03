package org.icgc.dcc.sodalite.server.repository;

import org.icgc.dcc.sodalite.server.repository.mapper.FileMapper;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.File;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
@RegisterMapper(FileMapper.class)
public interface FileRepository {
	  @SqlUpdate("INSERT INTO File (id, sample_id, name, size, md5sum, type) VALUES (:id, :sample_id, :name, :size, :md5sum, :file_type)")
	  int save(@Bind("id") String id, @Bind("sample_id") String sample_id, @Bind("name") String name, 
			  @Bind("size") Long size, @Bind("md5sum") String md5sum,
			  @Bind("file_type") String fileType);
	  
	  @SqlUpdate("UPDATE File SET id=:id,sample_id=:sample_id,name=:name,size=:size,md5um=:md5sum,type=:file_type")
	  int set(@Bind("id") String id, @Bind("sample_id") String sample_id, @Bind("name") String name, 
			  @Bind("size") Long size, @Bind("md5sum") String md5sum,
			  @Bind("file_type") String fileType);
	  
	  @SqlUpdate("DELETE From File where id=:id")
	  int delete(@Bind("id") String id);
	  
	  @SqlUpdate("DELETE From File where sample_id=:sample_id")
	  int deleteBySampleId(@Bind("sample_id") String sample_id);

	  @SqlQuery("SELECT id, name, size, md5sum, type FROM File WHERE id=:id")
	  File getById(@Bind("id") String id);
	  
	  @SqlQuery("SELECT id, name, size, md5sum, type FROM File WHERE sample_id=:sample_id")
	  List<File> getBySampleId(@Bind("sample_id") String sample_id);
	  
	  
}
