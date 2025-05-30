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

import static org.mapstruct.NullValuePropertyMappingStrategy.SET_TO_DEFAULT;

import bio.overture.song.core.model.Metadata;
import bio.overture.song.server.config.ConverterConfig;
import bio.overture.song.server.model.entity.FileEntity;
import java.util.Collection;
import java.util.List;
import lombok.val;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    config = ConverterConfig.class,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PayloadConverter {

  @Mapping(target = "analysisId", ignore = true)
  @Mapping(target = "objectId", ignore = true)
  @Mapping(target = "studyId", ignore = true)
  @Mapping(target = "info", ignore = true)
  @Mapping(target = "dataType", nullValuePropertyMappingStrategy = SET_TO_DEFAULT)
  @Mapping(target = "fileType", nullValuePropertyMappingStrategy = SET_TO_DEFAULT)
  @Mapping(target = "fileAccess", nullValuePropertyMappingStrategy = SET_TO_DEFAULT)
  void updateFile(FileEntity ref, @MappingTarget FileEntity entityToUpdate);

  default void updateInfo(Metadata ref, @MappingTarget Metadata metadataToUpdate) {
    metadataToUpdate.setInfo(ref.getInfo());
  }

  default FileEntity convertToFileEntityPayload(FileEntity ref) {
    val c = new FileEntity();
    updateFile(ref, c);
    updateInfo(ref, c);
    return c;
  }

  List<FileEntity> convertToFilePayloads(Collection<FileEntity> files);
}
