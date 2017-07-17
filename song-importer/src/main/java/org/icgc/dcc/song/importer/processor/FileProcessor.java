package org.icgc.dcc.song.importer.processor;

import lombok.val;
import org.icgc.dcc.song.importer.convert.Converters;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.model.SampleSet;
import org.icgc.dcc.song.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.UploadRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.groupingBy;

public class FileProcessor implements Runnable {

  private final List<PortalFileMetadata> portalFileMetadatas;
  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private UploadRepository uploadRepository;

  private final Set<SampleSet> sampleSetSet = newHashSet();
  private final Map<String, List<PortalFileMetadata>> dataBundleIdMap;

  private FileProcessor(List<PortalFileMetadata> portalFileMetadatas){
    this.portalFileMetadatas = portalFileMetadatas;
    this.dataBundleIdMap = groupByBundleId(portalFileMetadatas);
  }

  @Override
  public void run() {
    portalFileMetadatas.forEach(this::updateRepos);
  }

  private static Map<String, List<PortalFileMetadata>> groupByBundleId(List<PortalFileMetadata> portalFileMetadataList){
    return portalFileMetadataList
        .stream()
        .collect(
            groupingBy(PortalFileMetadata::getRepoDataBundleId));
  }

  private void updateRepos(PortalFileMetadata portalFileMetadata){
    val file = updateFile(portalFileMetadata);
    val analysis = updateAnalysis(portalFileMetadata);
    updateUpload(portalFileMetadata);
    updateFileSetTable(analysis, file);
    updateSampleSetTable(portalFileMetadata);
    updateVariantCall(portalFileMetadata);
  }

  private void updateUpload(PortalFileMetadata portalFileMetadata){
    val upload = Converters.convertToUpload(portalFileMetadata);
    uploadRepository.create(upload);
  }

  private Optional<Analysis> updateVariantCall(PortalFileMetadata portalFileMetadata){
    val fileType = FileTypes.resolve(portalFileMetadata);
    if (fileType == FileTypes.VCF) {
      val variantCallAnalysis = Converters.convertToVariantCallAnalysis(portalFileMetadata);
      analysisRepository.createVariantCall(variantCallAnalysis.getExperiment());
      analysisRepository.createAnalysis(variantCallAnalysis);
      return Optional.of(variantCallAnalysis);
    }
    return Optional.empty();

  }

  private Optional<Analysis> updateSequencingReadAnalysis(PortalFileMetadata portalFileMetadata){
    val fileType = FileTypes.resolve(portalFileMetadata);
    if (fileType == FileTypes.BAM){
      val sequencingReadAnalysis = Converters.convertToSequencingReadAnalysis(portalFileMetadata);
      analysisRepository.createSequencingRead(sequencingReadAnalysis.getExperiment());
      analysisRepository.createAnalysis(sequencingReadAnalysis);
      return Optional.of(sequencingReadAnalysis);
    }
    return Optional.empty();
  }

  private void updateSampleSetTable(PortalFileMetadata portalFileMetadata){
    for (val sampleEntry : extractSampleEntries(portalFileMetadata)){
      if (!sampleSetSet.contains(sampleEntry)){
        analysisRepository.addSample(sampleEntry.getAnalysisId(), sampleEntry.getSampleId());
        sampleSetSet.add(sampleEntry);
      }
    }
  }

  private void updateFileSetTable(Analysis analysis, File file){
      analysisRepository.addFile(analysis.getAnalysisId(), file.getObjectId());
  }

  private Analysis updateAnalysis(PortalFileMetadata portalFileMetadata){
    val optSeq = updateSequencingReadAnalysis(portalFileMetadata);
    val optVar = updateVariantCall(portalFileMetadata);
    val presenceMutuallyExcl = optSeq.isPresent() == !optVar.isPresent();
    checkState(presenceMutuallyExcl, "Cannot have a SequencingRead and VariantRead preset at the same time");
    return optSeq.orElseGet(optVar::get);
  }

  private File updateFile(PortalFileMetadata portalFileMetadata){
//    val file = convertToSpecimenSampleTuple(portalFileMetadata);
//    fileRepository.create(file);
//    return file;
    return null;
  }

  public static FileProcessor createFileProcessor(List<PortalFileMetadata> portalFileMetadatas){
    return new FileProcessor(portalFileMetadatas);
  }

  private static List<SampleSet> extractSampleEntries(PortalFileMetadata portalFileMetadata){
//    val analysis = convertToAnalysis(portalFileMetadata);
//    val analysisId = analysis.getAnalysisId();
//    return portalFileMetadata.getSampleIds().stream()
//        .map(x -> SampleSet.createSampleEntry(analysisId, x))
//        .collect(toImmutableList());
    return null; //TODO: rtismaHACK
  }

}
