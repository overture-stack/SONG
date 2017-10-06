package org.icgc.dcc.song.importer.convert;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.icgc.dcc.song.importer.dao.DonorDao;
import org.icgc.dcc.song.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.resolvers.AnalysisTypes;
import org.icgc.dcc.song.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.convert.FileConverter.getFileTypes;
import static org.icgc.dcc.song.importer.convert.StudyConverter.getStudyId;
import static org.icgc.dcc.song.importer.resolvers.AnalysisTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.importer.resolvers.AnalysisTypes.VARIANT_CALL;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.resolveAccessType;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.PUBLISHED;

@RequiredArgsConstructor
public class AnalysisConverter {

  private static final String NA = "";
  private static final String ALIGNED_READS = "Aligned Reads";
  private static final long NULL_INSERT_SIZE = -1;
  private static final boolean DEFAULT_SEQUENCING_READ_IS_PAIRED = false;

  private final DonorDao donorDao;

  public List<SequencingReadAnalysis> convertSequencingReads(@NonNull List<PortalFileMetadata> portalFileMetadatas){
    val aggSet = portalFileMetadatas.stream()
        .filter(x -> getFileTypes(x) == FileTypes.BAM)
        .map(this::buildSeqReadAggregate)
        .collect(toImmutableSet());
    return aggSet.stream()
        .map(AnalysisConverter::buildSequencingReadAnalysis)
        .collect(toImmutableList());
  }

  public List<VariantCallAnalysis> convertVariantCalls(@NonNull List<PortalFileMetadata> portalFileMetadatas){
    val aggSet = portalFileMetadatas.stream()
        .filter(x -> getFileTypes(x) == FileTypes.VCF)
        .map(this::buildVariantCallAggregate)
        .collect(toImmutableSet());
    return aggSet.stream()
        .map(AnalysisConverter::buildVariantCallAnalysis)
        .collect(toImmutableList());
  }

  private VariantCallAggregate buildVariantCallAggregate(PortalFileMetadata portalFileMetadata){
    val portalDonorMetadata = donorDao.getPortalDonorMetadata(portalFileMetadata.getDonorId());
    return VariantCallAggregate.builder()
        .studyId(getStudyId(portalFileMetadata))
        .analysisId(getAnalysisId(portalFileMetadata))
        .variantCallingTool(getVariantCallingTool(portalFileMetadata))
        .type(getAnalysisType(portalFileMetadata))
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

    val analysisType = AnalysisTypes.resolve(variantCallAggregate.getType());
    checkState(analysisType == VARIANT_CALL,
        "The input VariantCallAggregate [%s] is NOT of analysisType [%s]",
        variantCallAggregate, VARIANT_CALL.getAnalysisTypeName());

    val variantCallAnalysis = new VariantCallAnalysis();
    val analysisId = variantCallAggregate.getAnalysisId();
    variantCallAnalysis.setAnalysisId(analysisId);
    variantCallAnalysis.setAnalysisState(PUBLISHED.toString());
    variantCallAnalysis.setAnalysisSubmitterId(analysisId);
    variantCallAnalysis.setStudy(variantCallAggregate.getStudyId());
    variantCallAnalysis.setInfo(NA);

    val variantCallExperiment = VariantCall.create(
        analysisId,
        variantCallAggregate.getVariantCallingTool(),
        variantCallAggregate.getMatchedNormalSampleSubmitterId());
    variantCallAnalysis.setExperiment(variantCallExperiment);
    return variantCallAnalysis;
  }

  private static SequencingReadAnalysis buildSequencingReadAnalysis(SeqReadAggregate seqReadAggregate) {

    val analysisType = AnalysisTypes.resolve(seqReadAggregate.getType());
    checkState(analysisType == SEQUENCING_READ,
        "The input SeqReadAggregate[%s] is NOT of analysisType [%s]",
        seqReadAggregate, SEQUENCING_READ.getAnalysisTypeName());

    val sequencingReadAnalysis = new SequencingReadAnalysis();
    val analysisId = seqReadAggregate.getAnalysisId();
    sequencingReadAnalysis.setAnalysisId(analysisId);
    sequencingReadAnalysis.setAnalysisState(PUBLISHED.toString());
    sequencingReadAnalysis.setAnalysisSubmitterId(analysisId);
    sequencingReadAnalysis.setStudy(seqReadAggregate.getStudyId());
    sequencingReadAnalysis.setInfo(NA);

    val sequencingReadExperiment = SequencingRead.create(
        seqReadAggregate.getAnalysisId(),
        seqReadAggregate.isAligned(),
        seqReadAggregate.getAlignmentTool(),
        NULL_INSERT_SIZE,
        seqReadAggregate.getLibraryStrategy(),
        DEFAULT_SEQUENCING_READ_IS_PAIRED,
        seqReadAggregate.getReferenceGenome());

    sequencingReadAnalysis.setExperiment(sequencingReadExperiment);
    return sequencingReadAnalysis;
  }

  public static Boolean isAligned(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getDataType().equals(ALIGNED_READS);
  }

  public static String getAnalysisId(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getRepoDataBundleId();
  }

  public static String getAnalysisType(@NonNull PortalFileMetadata portalFileMetadata){
    return AnalysisTypes.resolve(FileTypes.resolve(portalFileMetadata)).getAnalysisTypeName();
  }

  public static String getReferenceGenome(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getGenomeBuild();
  }

  public static String getLibraryStrategy(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getExperimentalStrategy();
  }

  public static String getAlignmentTool(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getSoftware();
  }

  public static String getVariantCallingTool(@NonNull PortalFileMetadata portalFileMetadata){
    return portalFileMetadata.getSoftware();
  }

  public static String getMatchedNormalSampleSubmitterId(@NonNull PortalDonorMetadata portalDonorMetadata){
    return portalDonorMetadata.getNormalAnalyzedId();
  }

  public static AccessTypes getAnalysisAccess(@NonNull PortalFileMetadata portalFileMetadata){
    return resolveAccessType(portalFileMetadata.getAccess());
  }

  public static AnalysisConverter createAnalysisConverter(DonorDao donorDao) {
    return new AnalysisConverter(donorDao);
  }

  @Builder
  @Value
  public static class VariantCallAggregate {

    @NonNull private final String analysisId;
    @NonNull private final String variantCallingTool;
    @NonNull private final String matchedNormalSampleSubmitterId;
    @NonNull private final String studyId;
    @NonNull private final String type;

  }

  @Builder
  @Value
  public static class SeqReadAggregate {

    @NonNull private final String analysisId;
    @NonNull private final String libraryStrategy;
    @NonNull private final String alignmentTool;
    @NonNull private final String referenceGenome;
    @NonNull private final String studyId;
    @NonNull private final String type;
    private final boolean aligned;

  }

}
