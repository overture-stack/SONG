package org.icgc.dcc.song.server.importer.convert;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.importer.dao.DonorDao;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.resolvers.AnalysisTypes;
import org.icgc.dcc.song.server.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.server.importer.convert.StudyConverter.getStudyId;
import static org.icgc.dcc.song.server.importer.resolvers.FileTypes.BAM;
import static org.icgc.dcc.song.server.importer.resolvers.FileTypes.VCF;
import static org.icgc.dcc.song.server.model.Upload.PUBLISHED;

@RequiredArgsConstructor
public class AnalysisConverter {

  private static final String NA = "";
  private static final String ALIGNED_READS = "Aligned Reads";
  private static final long NULL_INSERT_SIZE = -1;
  private static final boolean DEFAULT_SEQUENCING_READ_IS_PAIRED = false;

  private final List<PortalFileMetadata> portalFileMetadatas;
  private final DonorDao donorDao;

  public List<SequencingReadAnalysis> convertSequencingReads(){
    val aggSet = portalFileMetadatas.stream()
        .map(this::buildSeqReadAggregate)
        .collect(toImmutableSet());
    return aggSet.stream()
        .map(AnalysisConverter::buildSequencingReadAnalysis)
        .collect(toImmutableList());
  }

  public List<VariantCallAnalysis> convertVariantCalls(){
    val aggSet = portalFileMetadatas.stream()
        .map(this::buildVariantCallAggregate)
        .collect(toImmutableSet());
    return aggSet.stream()
        .map(AnalysisConverter::buildVariantCallAnalysis)
        .collect(toImmutableList());
  }

  private VariantCallAggregate buildVariantCallAggregate(PortalFileMetadata portalFileMetadata){
    val portalDonorMetadata = donorDao.getPortalDonorMetadata(portalFileMetadata.getDonorId());
    return VariantCallAggregate.builder()
        .analysisId(getAnalysisId(portalFileMetadata))
        .variantCallingTool(getVariantCallingTool(portalFileMetadata))
        .matchedNormalSampleSubmitterId(getMatchedNormalSampleSubmitterId(portalDonorMetadata))
        .build();
  }

  private SeqReadAggregate buildSeqReadAggregate(PortalFileMetadata portalFileMetadata){
    return SeqReadAggregate.builder()
        .analysisId(getAnalysisId(portalFileMetadata))
        .type(getAnalysisType(portalFileMetadata))
        .studyId(getStudyId(portalFileMetadata))
        .referenceGenome(getReferenceGenome(portalFileMetadata))
        .libraryStrategy(getLibraryStrategy(portalFileMetadata))
        .alignmentTool(getAlignmentTool(portalFileMetadata))
        .aligned(isAligned(portalFileMetadata))
        .build();
  }

  private static VariantCallAnalysis buildVariantCallAnalysis(VariantCallAggregate variantCallAggregate) {

    val fileType = FileTypes.resolve(variantCallAggregate.getType());
    checkState(fileType == VCF,
        "The input VariantCallAggregate %s is NOT of fileType [%s]",
        variantCallAggregate.toString(), VCF.getFileTypeName());

    val variantCallAnalysis = new VariantCallAnalysis();
    updateAnalysis(variantCallAnalysis, variantCallAggregate);

    val variantCallExperiment = VariantCall.create(
        variantCallAggregate.getAnalysisId(),
        variantCallAggregate.getVariantCallingTool(),
        variantCallAggregate.getMatchedNormalSampleSubmitterId(),
        NA);
    variantCallAnalysis.setExperiment(variantCallExperiment);
    return variantCallAnalysis;
  }

  private static SequencingReadAnalysis buildSequencingReadAnalysis(SeqReadAggregate seqReadAggregate) {
    val fileType = FileTypes.resolve(seqReadAggregate.getType());

    checkState(fileType == BAM,
        "The input SeqReadAggregate %s is NOT of fileType [%s]",
        seqReadAggregate.toString(), BAM.getFileTypeName());

    val sequencingReadAnalysis = new SequencingReadAnalysis();
    updateAnalysis(sequencingReadAnalysis, seqReadAggregate);


    val sequencingReadExperiment = SequencingRead.create(
        seqReadAggregate.getAnalysisId(),
        seqReadAggregate.isAligned(),
        seqReadAggregate.getAlignmentTool(),
        NULL_INSERT_SIZE,
        seqReadAggregate.getLibraryStrategy(),
        DEFAULT_SEQUENCING_READ_IS_PAIRED,
        seqReadAggregate.getReferenceGenome(),
        NA);

    sequencingReadAnalysis.setExperiment(sequencingReadExperiment);
    return sequencingReadAnalysis;
  }

  private static void updateAnalysis(Analysis analysis, SeqReadAggregate seqReadAggregate){
    analysis.setAnalysisId(seqReadAggregate.getAnalysisId());
    analysis.setAnalysisState(PUBLISHED);
    analysis.setStudy(seqReadAggregate.getStudyId());
    analysis.setInfo(NA);
  }

  private static void updateAnalysis(Analysis analysis, VariantCallAggregate variantCallAggregate){
    analysis.setAnalysisId(variantCallAggregate.getAnalysisId());
    analysis.setAnalysisState(PUBLISHED);
    analysis.setStudy(variantCallAggregate.getStudyId());
    analysis.setInfo(NA);
  }


  public static Boolean isAligned(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getDataType().equals(ALIGNED_READS);
  }

  public static String getAnalysisId(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getRepoDataBundleId();
  }

  public static String getAnalysisType(PortalFileMetadata portalFileMetadata){
    return AnalysisTypes.resolve(FileTypes.resolve(portalFileMetadata)).getAnalysisTypeName();
  }

  public static String getReferenceGenome(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getGenomeBuild();
  }

  public static String getLibraryStrategy(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getExperimentalStrategy();
  }

  public static String getAlignmentTool(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getSoftware();
  }

  public static String getVariantCallingTool(PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getSoftware();
  }

  public static String getMatchedNormalSampleSubmitterId(PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getNormalAnalyzedId();
  }

}
