package bio.overture.song.server.converter;

import bio.overture.song.server.config.ConverterConfig;
import bio.overture.song.server.model.Metadata;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.dto.Payload;
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

import static org.mapstruct.NullValuePropertyMappingStrategy.SET_TO_DEFAULT;
import static bio.overture.song.server.service.AnalysisTypeService.resolveAnalysisTypeId;

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
  @Mapping(target = "fileType", nullValuePropertyMappingStrategy = SET_TO_DEFAULT)
  @Mapping(target = "fileAccess", nullValuePropertyMappingStrategy = SET_TO_DEFAULT)
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

  default Payload convertToPayload(Analysis a, boolean includeAnalysisIds) {
    val payload = Payload.builder()
        .analysisId(includeAnalysisIds? a.getAnalysisId() : null)
        .analysisTypeId(resolveAnalysisTypeId(a.getAnalysisSchema()))
        .study(a.getStudy())
        .sample(convertToSamplePayloads(a.getSample()))
        .file(convertToFilePayloads(a.getFile()))
        .build();
    payload.addData(a.getAnalysisData().getData());
    return payload;
  }
}
