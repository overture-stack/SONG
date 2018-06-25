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

package org.icgc.dcc.song.server.converter;

import org.icgc.dcc.song.server.model.ScoreObject;
import org.icgc.dcc.song.server.model.entity.file.FileData;
import org.icgc.dcc.song.server.model.entity.file.impl.File;
import org.icgc.dcc.song.server.model.entity.file.impl.FileUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FileConverter {

  FileUpdateRequest fileEntityToFileUpdateRequest(File file);
  void updateEntityFromData(FileData fileData, @MappingTarget File file);
  File copyFile(File file);
  List<File> copyFiles(List<File> files);

  default ScoreObject toScoreObject(File file){
    return ScoreObject.builder()
        .objectId(file.getObjectId())
        .fileSize(file.getFileSize())
        .fileMd5sum(file.getFileMd5sum())
        .build();
  }

}
