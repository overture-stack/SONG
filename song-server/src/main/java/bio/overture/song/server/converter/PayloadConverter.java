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

import bio.overture.song.core.model.Metadata;
import bio.overture.song.server.config.ConverterConfig;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import lombok.val;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collection;
import java.util.List;

@Mapper(
    config = ConverterConfig.class,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PayloadConverter {

  @Mapping(target = "sampleId", ignore = true)
  @Mapping(target = "specimenId", ignore = true)
  @Mapping(target = "withSample", ignore = true)
  @Mapping(target = "specimen", ignore = true)
  @Mapping(target = "donor", ignore = true)
  @Mapping(target = "info", ignore = true)
  void updateCompositeEntity(CompositeEntity ref, @MappingTarget CompositeEntity entityToUpdate);

  @Mapping(target = "donorId", ignore = true)
  @Mapping(target = "specimenId", ignore = true)
  @Mapping(target = "withSpecimen", ignore = true)
  @Mapping(target = "info", ignore = true)
  void updateSpecimen(Specimen ref, @MappingTarget Specimen entityToUpdate);

  @Mapping(target = "donorId", ignore = true)
  @Mapping(target = "studyId", ignore = true)
  @Mapping(target = "withDonor", ignore = true)
  @Mapping(target = "info", ignore = true)
  void updateDonor(Donor ref, @MappingTarget Donor entityToUpdate);

  @Mapping(target = "analysisId", ignore = true)
  @Mapping(target = "objectId", ignore = true)
  @Mapping(target = "studyId", ignore = true)
  @Mapping(target = "info", ignore = true)
  @Mapping(target = "fileType")
  @Mapping(target = "fileAccess")
  void updateFile(FileEntity ref, @MappingTarget FileEntity entityToUpdate);

  default void updateInfo(Metadata ref, @MappingTarget Metadata metadataToUpdate) {
    metadataToUpdate.setInfo(ref.getInfo());
  }

  default CompositeEntity convertToSamplePayload(CompositeEntity ref) {
    val c = new CompositeEntity();
    updateCompositeEntity(ref, c);
    updateInfo(ref, c);
    c.setDonor(convertToDonorPayload(ref.getDonor()));
    c.setSpecimen(convertToSpecimenPayload(ref.getSpecimen()));
    return c;
  }

  default Specimen convertToSpecimenPayload(Specimen ref) {
    val c = new Specimen();
    updateSpecimen(ref, c);
    updateInfo(ref, c);
    return c;
  }

  default FileEntity convertToFileEntityPayload(FileEntity ref) {
    val c = new FileEntity();
    updateFile(ref, c);
    updateInfo(ref, c);
    return c;
  }

  default Donor convertToDonorPayload(Donor ref) {
    val c = new Donor();
    updateDonor(ref, c);
    updateInfo(ref, c);
    return c;
  }

  List<CompositeEntity> convertToSamplePayloads(Collection<CompositeEntity> samples);

  List<FileEntity> convertToFilePayloads(Collection<FileEntity> files);
}
