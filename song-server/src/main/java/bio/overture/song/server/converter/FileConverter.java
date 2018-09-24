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

package bio.overture.song.server.converter;

import bio.overture.song.server.model.StorageObject;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.core.model.file.FileData;
import bio.overture.song.core.model.file.FileUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FileConverter {

  FileUpdateRequest fileEntityToFileUpdateRequest(FileEntity file);
  void updateEntityFromData(FileData fileData, @MappingTarget FileEntity file);
  FileEntity copyFile(FileEntity file);
  List<FileEntity> copyFiles(List<FileEntity> files);

  default StorageObject toStorageObject(FileEntity file){
    return StorageObject.builder()
        .objectId(file.getObjectId())
        .fileSize(file.getFileSize())
        .fileMd5sum(file.getFileMd5sum())
        .build();
  }

}
