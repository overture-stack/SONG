package org.icgc.dcc.song.server.converter;

import org.icgc.dcc.song.schema.FileOuterClass.File;
import org.mapstruct.Mapper;

@Mapper(uses = FileMapper.BuilderFactory.class)
public interface FileMapper {

  File.Builder convertToProtobufFile(org.icgc.dcc.song.server.model.entity.File file);

  class BuilderFactory{
    File.Builder builder(){
      return File.newBuilder();
    }

  }

}
