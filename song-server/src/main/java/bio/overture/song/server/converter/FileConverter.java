/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

package bio.overture.song.server.converter;

import static org.mapstruct.factory.Mappers.getMapper;

import bio.overture.song.core.model.FileData;
import bio.overture.song.core.model.FileUpdateRequest;
import bio.overture.song.server.config.ConverterConfig;
import bio.overture.song.server.model.StorageObject;
import bio.overture.song.server.model.entity.FileEntity;
import java.util.List;
import lombok.val;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    config = ConverterConfig.class,
    nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FileConverter {

  FileConverter INSTANCE = getMapper(FileConverter.class);

  FileUpdateRequest fileEntityToFileUpdateRequest(FileEntity file);

  @Mapping(target = "fileType", ignore = true)
  @Mapping(target = "objectId", ignore = true)
  @Mapping(target = "studyId", ignore = true)
  @Mapping(target = "analysisId", ignore = true)
  @Mapping(target = "fileName", ignore = true)
  void updateEntityFromData(FileData fileData, @MappingTarget FileEntity file);

  void updateFileEntity(FileEntity ref, @MappingTarget FileEntity fileToUpdate);

  // NOTE: mapstruct cannot properly generate this method because it defaults to using the builder
  // instead of the setters. Since the info field is not apart of the builder, it does not fully
  // copy.
  default FileEntity copyFile(FileEntity ref) {
    val copy = new FileEntity();
    updateFileEntity(ref, copy);
    return copy;
  }

  List<FileEntity> copyFiles(List<FileEntity> files);

  default StorageObject toStorageObject(FileEntity file) {
    return StorageObject.builder()
        .objectId(file.getObjectId())
        .fileSize(file.getFileSize())
        .fileMd5sum(file.getFileMd5sum())
        .build();
  }
}
