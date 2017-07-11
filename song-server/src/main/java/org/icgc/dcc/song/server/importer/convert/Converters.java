package org.icgc.dcc.song.server.importer.convert;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import lombok.Lombok;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.model.PortalSampleMetadata;
import org.icgc.dcc.song.server.importer.model.PortalSpecimenMetadata;
import org.icgc.dcc.song.server.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.importer.resolvers.SampleTypes;
import org.icgc.dcc.song.server.importer.resolvers.SpecimenClasses;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
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
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getRepoDataBundleId;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getSampleIds;
import static org.icgc.dcc.song.server.importer.convert.PortalFileMetadataParser.getSoftware;
import static org.icgc.dcc.song.server.importer.resolvers.FileTypes.BAM;

@Slf4j
public class Converters {
  private static final String NA = "";
  private static final String ALIGNED_READS = "Aligned Reads";
  private static final long NULL_INSERT_SIZE = -1;
  private static final boolean IS_PAIRED = true;
  private static final String DONOR_GENDER_DEFAULT = "unspecified";
  private static final String EMPTY_STRING = "";

  public static Donor convertToDonor(PortalDonorMetadata portalDonorMetadata){
    return Donor.create(portalDonorMetadata.getId(),
        portalDonorMetadata.getSubmitterDonorId().orElse(EMPTY_STRING),
        portalDonorMetadata.getProjectId(),
        portalDonorMetadata.getGender().orElse(DONOR_GENDER_DEFAULT),
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

  public static Stream<Sample> streamToSamples(PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getSpecimens().stream()
        .map(Converters::convertToSamples)
        .flatMap(Collection::stream);
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
    return File.create(
        portalFileMetadata.getFileId(),
        portalFileMetadata.getFileName(),
        portalFileMetadata.getProjectCode(),
        portalFileMetadata.getFileSize(),
        portalFileMetadata.getFileFormat(),
        portalFileMetadata.getFileMd5sum(),
        NA);
  }


  private static Boolean isAligned(String dataType){
    return dataType.equals(ALIGNED_READS);
  }

  public static SequencingRead convertToSequencingRead(PortalFileMetadata portalFileMetadata){
    throw new IllegalStateException("not implemented");
  }

  public static SequencingReadAnalysis convertToSequencingReadAnalysis(PortalFileMetadata portalFileMetadata){
    val fileType = FileTypes.resolve(portalFileMetadata);
    checkState(fileType == BAM, "The input PortalFileMetadata %s is NOT of fileType [%s]", portalFileMetadata.toString(), BAM.getFileTypeName());
    val dataBundleId = portalFileMetadata.getRepoDataBundleId();
    val sequencingReadAnalysis = SequencingReadAnalysis.create(
        dataBundleId,
        portalFileMetadata.getProjectCode(),
        Upload.PUBLISHED,
        NA);

    val sequencingReadExperiment = SequencingRead.create(
        dataBundleId,
        isAligned(portalFileMetadata.getDataType()),
        portalFileMetadata.getSoftware(),
        NULL_INSERT_SIZE,
        portalFileMetadata.getExperimentalStrategy(),
        IS_PAIRED,
        portalFileMetadata.getGenomeBuild(),
        NA);
    sequencingReadAnalysis.setExperiment(sequencingReadExperiment);
    return sequencingReadAnalysis;
  }

  public static VariantCall convertToVariantCall(PortalFileMetadata portalFileMetadata){
    throw new IllegalStateException("not implemented");
  }

  public static Upload convertToUpload(PortalFileMetadata portalFileMetadata){
    throw new IllegalStateException("not implemented");
  }

  public static Study convertToStudy(PortalDonorMetadata portalDonorMetadata){
    throw new IllegalStateException("not implemented");
  }

  public static PortalFileMetadata convertToPortalFileMetadata(ObjectNode o){
    try {

    return PortalFileMetadata.builder()
        .access              (getAccess(o))
        .repoDataBundleId    (getRepoDataBundleId(o))
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
        .indexFileFileFormat (getIndexFileFileFormat(o).orElse(null))
        .indexFileFileMd5sum (getIndexFileFileMd5sum(o).orElse(null))
        .indexFileFileName   (getIndexFileFileName(o).orElse(null))
        .indexFileFileSize   (getIndexFileFileSize(o).orElse(null))
        .indexFileId         (getIndexFileId(o).orElse(null))
        .indexFileObjectId   (getIndexFileObjectId(o).orElse(null))
        .objectId            (getObjectId(o))
        .projectCode         (getProjectCode(o))
        .sampleIds           (getSampleIds(o))
        .software            (getSoftware(o))
        .build();
    } catch (Throwable t){
      log.error("OBJECT_DATA:\n{}",JsonUtils.toPrettyJson(o));
      throw Lombok.sneakyThrow(t);
    }
  }

  public static PortalDonorMetadata convertToPortalDonorMetadata(ObjectNode donor){
    try{

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
    } catch(Throwable t){
      log.info("OBJECT_DATA_DUMP:\n{}", JsonUtils.toPrettyJson(donor));
      throw Lombok.sneakyThrow(t);
    }
  }

}
