package org.icgc.dcc.song.importer.convert;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalDonorMetadataOLD;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.model.PortalSampleMetadata;
import org.icgc.dcc.song.importer.model.PortalSpecimenMetadata;
import org.icgc.dcc.song.importer.resolvers.FileTypes;
import org.icgc.dcc.song.importer.resolvers.SpecimenClasses;
import org.icgc.dcc.song.importer.resolvers.SampleTypes;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.song.server.model.Upload.PUBLISHED;

@Slf4j
public class Converters {
  public static final String NA = "";
  private static final String ALIGNED_READS = "Aligned Reads";
  private static final long NULL_INSERT_SIZE = -1;
  private static final boolean IS_PAIRED = true;
  private static final String DONOR_GENDER_DEFAULT = "unspecified";
  private static final boolean DEFAULT_SEQUENCING_READ_IS_PAIRED = false;
  private static final String EMPTY_STRING = "";

  public static Donor convertToDonor(PortalFileMetadata portalFileMetadata, PortalDonorMetadata portalDonorMetadata){
    return Donor.create(
        portalFileMetadata.getDonorId(),
        portalFileMetadata.getSubmittedDonorId(),
        portalFileMetadata.getProjectCode(),
        portalDonorMetadata.getGender().orElse(DONOR_GENDER_DEFAULT),
        NA);
  }

  public static List<Specimen> convertToSpecimens(PortalDonorMetadataOLD portalDonorMetadata){
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
    return SpecimenClasses.resolve(portalSpecimenMetadata).getSpecimenClassName();
  }

  private static String resolveSampleType(PortalSampleMetadata portalSampleMetadata){
    return SampleTypes.resolve(portalSampleMetadata).getSampleTypeName();
  }

  public static Stream<Sample> streamToSamples(PortalDonorMetadataOLD portalDonorMetadata){
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

  private static Boolean isAligned(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getDataType().equals(ALIGNED_READS);
  }

  private static String getAlignmentTool(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getSoftware();
  }

  private static String getVariantCallingTool(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getSoftware();
  }

  private static String getLibraryStrategy(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getExperimentalStrategy();
  }

  private static String getReferenceGenome(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getGenomeBuild();
  }

  public static SequencingReadAnalysis convertToSequencingReadAnalysis(PortalFileMetadata portalFileMetadata){
    val fileType = FileTypes.resolve(portalFileMetadata);

    checkState(fileType == FileTypes.BAM,
        "The input PortalFileMetadata %s is NOT of fileType [%s]",
        portalFileMetadata.toString(), FileTypes.BAM.getFileTypeName());

    val sequencingReadAnalysis = new SequencingReadAnalysis();
    updateAnalysis(sequencingReadAnalysis, portalFileMetadata);

    val sequencingReadExperiment = SequencingRead.create(
        getStudyId(portalFileMetadata),
        isAligned(portalFileMetadata),
        getAlignmentTool(portalFileMetadata),
        NULL_INSERT_SIZE,
        getLibraryStrategy(portalFileMetadata),
        DEFAULT_SEQUENCING_READ_IS_PAIRED,
        getReferenceGenome(portalFileMetadata),
        NA);

    sequencingReadAnalysis.setExperiment(sequencingReadExperiment);
    return sequencingReadAnalysis;
  }

  public static String getStudyId(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getProjectCode();
  }
  public static String getAnalysisId(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getRepoDataBundleId();
  }

  private static void updateAnalysis(Analysis analysis, PortalFileMetadata portalFileMetadata ){
    analysis.setAnalysisId(getAnalysisId(portalFileMetadata));
    analysis.setAnalysisState(PUBLISHED);
    analysis.setStudy(getStudyId(portalFileMetadata));
    analysis.setInfo(NA);
  }

  public static VariantCallAnalysis convertToVariantCallAnalysis(PortalFileMetadata portalFileMetadata){
    val fileType = FileTypes.resolve(portalFileMetadata);
    checkState(fileType == FileTypes.VCF,
        "The input PortalFileMetadata %s is NOT of fileType [%s]",
        portalFileMetadata.toString(), FileTypes.VCF.getFileTypeName());

    val variantCallAnalysis = new VariantCallAnalysis();
    updateAnalysis(variantCallAnalysis, portalFileMetadata);

//    val variantCallExperiment = VariantCall.create(
//        getStudyId(portalFileMetadata),
//        getVariantCallingTool(portalFileMetadata),
//        get
//
//
//    );
    //TODO::sdfsdfsdf

    return null;



  }


  public static Upload convertToUpload(PortalFileMetadata portalFileMetadata){
    throw new IllegalStateException("not implemented");
  }

  public static Study convertToStudy(PortalDonorMetadataOLD portalDonorMetadata){
    throw new IllegalStateException("not implemented");
  }

}
