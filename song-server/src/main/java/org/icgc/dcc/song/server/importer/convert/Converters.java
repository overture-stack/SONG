package org.icgc.dcc.song.server.importer.convert;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import lombok.val;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.model.PortalSampleMetadata;
import org.icgc.dcc.song.server.importer.model.PortalSpecimenMetadata;
import org.icgc.dcc.song.server.importer.resolvers.SampleTypes;
import org.icgc.dcc.song.server.importer.resolvers.SpecimenClasses;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;

import java.util.List;

import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getGender;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getId;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getNumSamples;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getNumSpecimens;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getProjectId;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getProjectName;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getSampleAnalyzedId;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getSampleId;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getSampleLibraryStrategy;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getSampleStudy;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getSpecimenId;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getSpecimenSubmittedId;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getSpecimenType;
import static org.icgc.dcc.song.server.importer.convert.PortalDonorMetadataParser.getSubmitterDonorId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getAccess;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getDataBundleId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getDataType;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getDonorId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getExperimentalStrategy;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getFileFormat;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getFileId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getFileLastModified;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getFileMd5sum;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getFileName;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getFileSize;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getGenomeBuild;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getIndexFileFileFormat;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getIndexFileFileMd5sum;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getIndexFileFileName;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getIndexFileFileSize;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getIndexFileId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getIndexFileObjectId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getObjectId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getProjectCode;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getSampleIds;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getSoftware;

public class Converters {
  private static final String NA = "";

  public static Donor convertToDonor(PortalDonorMetadata portalDonorMetadata){
    return Donor.create(portalDonorMetadata.getId(),
        portalDonorMetadata.getSubmitterDonorId(),
        portalDonorMetadata.getProjectId(),
        portalDonorMetadata.getGender(),
        NA);
  }

  public static List<Specimen> convertToSpecimens(PortalDonorMetadata portalDonorMetadata){
    val donorId = portalDonorMetadata.getId();
    val specimens = ImmutableList.<Specimen>builder();
    for (val portalSpecimentMetadata : portalDonorMetadata.getSpecimens()){
      val specimen = Specimen.create(
          portalSpecimentMetadata.getId(),
          portalSpecimentMetadata.getSubmittedId(),
          donorId,
          resolveSpecimenClass(portalSpecimentMetadata),
          portalSpecimentMetadata.getType(),
          NA);
      specimens.add(specimen);
    }
    return specimens.build();
  }

  private static String resolveSpecimenClass(PortalSpecimenMetadata portalSpecimenMetadata){
    return SpecimenClasses.resolve(portalSpecimenMetadata).getDisplayName();
  }

  private static String resolveSampleType(PortalSampleMetadata portalSampleMetadata){
    return SampleTypes.resolve(portalSampleMetadata).getDisplayName();
  }

  public static List<Sample> convertToSamples(PortalSpecimenMetadata portalSpecimenMetadata){
    val specimenId = portalSpecimenMetadata.getId();
    val samples = ImmutableList.<Sample>builder();
    for(val portalSampleMetadata : portalSpecimenMetadata.getSamples()){
      val sample = Sample.create(
          portalSampleMetadata.getId(),
          portalSampleMetadata.getAnalyzedId(),
          specimenId,
          resolveSampleType(portalSampleMetadata),
          NA);
      samples.add(sample);
    }
    return samples.build();
  }

  public static File convertToFile(PortalFileMetadata portalFileMetadata){
  }

  public static Analysis convertToAnalysis(PortalFileMetadata portalFileMetadata){
  }

  public static SequencingRead convertToSequencingRead(PortalFileMetadata portalFileMetadata){
  }

  public static VariantCall convertToVariantCall(PortalFileMetadata portalFileMetadata){
  }

  public static Upload convertToUpload(PortalFileMetadata portalFileMetadata){
  }

  public static Study convertToStudy(PortalDonorMetadata portalDonorMetadata){
  }

  public static PortalFileMetadata convertToPortalFileMetadata(ObjectNode o){
    return PortalFileMetadata.builder()
        .access              (getAccess(o))
        .dataBundleId        (getDataBundleId(o))
        .dataType            (getDataType(o))
        .donorId             (getDonorId(o))
        .experimentalStrategy(getExperimentalStrategy(o))
        .fileFormat          (getFileFormat(o))
        .fileId              (getFileId(o))
        .fileLastModified    (getFileLastModified(o))
        .fileMd5sum          (getFileMd5sum(o))
        .fileName            (getFileName(o))
        .fileSize            (getFileSize(o))
        .genomeBuild         (getGenomeBuild(o))
        .indexFileFileFormat (getIndexFileFileFormat(o))
        .indexFileFileMd5sum (getIndexFileFileMd5sum(o))
        .indexFileFileName   (getIndexFileFileName(o))
        .indexFileFileSize   (getIndexFileFileSize(o))
        .indexFileId         (getIndexFileId(o))
        .indexFileObjectId   (getIndexFileObjectId(o))
        .objectId            (getObjectId(o))
        .projectCode         (getProjectCode(o))
        .sampleIds           (getSampleIds(o))
        .software            (getSoftware(o))
        .build();
  }

  public static PortalDonorMetadata convertToPortalDonorMetadata(ObjectNode donor){
    val donorBuilder = PortalDonorMetadata.builder()
        .gender(getGender(donor))
        .id(getId(donor))
        .projectId(getProjectId(donor))
        .submitterDonorId(getSubmitterDonorId(donor))
        .projectName(getProjectName(donor));

    val numSpecimens = getNumSpecimens(donor);
    for (int specimenIdx = 0; specimenIdx < numSpecimens; specimenIdx++){
      val specimenBuilder = PortalSpecimenMetadata.builder()
          .id(getSpecimenId(donor,specimenIdx))
          .submittedId(getSpecimenSubmittedId(donor, specimenIdx))
          .type(getSpecimenType(donor, specimenIdx));

      val numSamples = getNumSamples(donor,specimenIdx);
      for (int sampleIdx = 0; sampleIdx < numSamples; sampleIdx++){
        val sample = PortalSampleMetadata.builder()
            .id(getSampleId(donor, specimenIdx, sampleIdx))
            .analyzedId(getSampleAnalyzedId(donor, specimenIdx, sampleIdx))
            .study(getSampleStudy(donor, specimenIdx, sampleIdx))
            .libraryStrategy(getSampleLibraryStrategy(donor, specimenIdx, sampleIdx))
            .build();
        specimenBuilder.sample(sample);
      }
      donorBuilder.specimen(specimenBuilder.build());
    }
    return donorBuilder.build();
  }

}
