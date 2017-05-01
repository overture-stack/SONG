package org.icgc.dcc.sodalite.server.repository;

import org.icgc.dcc.sodalite.server.repository.mapper.FileMapper;
import org.icgc.dcc.sodalite.server.model.File;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
@RegisterMapper(FileMapper.class)
public interface FileRepository {
	  @SqlUpdate("INSERT INTO File (id, sample_id, name, size,md5sum,type) VALUES (:id, :sample_id, :name, :size, :md5sum, :file_type)")
	  int save(@Bind("id") String id, @Bind("sample_id") String sample_id, @Bind("name") String name, 
			  @Bind("size") Long size, @Bind("md5sum") String md5sum,
			  @Bind("file_type") String fileType);

	  @SqlQuery("SELECT id, sample_id,name, size, md5sum, type FROM File WHERE id=:id")
	  File getById(@Bind("id") String id);	
}
