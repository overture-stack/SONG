package org.icgc.dcc.song.server.importer.processor;

import lombok.val;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.model.SampleEntry;
import org.icgc.dcc.song.server.importer.resolvers.FileTypes;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.UploadRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.song.server.importer.convert.Converters.convertToFile;
import static org.icgc.dcc.song.server.importer.convert.Converters.convertToSequencingReadAnalysis;
import static org.icgc.dcc.song.server.importer.convert.Converters.convertToUpload;
import static org.icgc.dcc.song.server.importer.convert.Converters.convertToVariantCall;
import static org.icgc.dcc.song.server.importer.resolvers.FileTypes.BAM;
import static org.icgc.dcc.song.server.importer.resolvers.FileTypes.VCF;

public class FileProcessor implements Runnable {

  private final List<PortalFileMetadata> portalFileMetadatas;
  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private UploadRepository uploadRepository;

  private final Set<SampleEntry> sampleEntrySet = newHashSet();
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
    val upload = convertToUpload(portalFileMetadata);
    uploadRepository.create(upload);
  }

  private void updateVariantCall(PortalFileMetadata portalFileMetadata){
    val fileType = FileTypes.resolve(portalFileMetadata);
    if (fileType == VCF) {
      val variantCall = convertToVariantCall(portalFileMetadata);
      analysisRepository.createVariantCall(variantCall);
    }
  }

  private void updateSequencingRead(PortalFileMetadata portalFileMetadata){
    val fileType = FileTypes.resolve(portalFileMetadata);
    if (fileType == BAM){
      val sequencingReadAnalysis = convertToSequencingReadAnalysis(portalFileMetadata);
      analysisRepository.createSequencingRead(sequencingReadAnalysis.getExperiment());
      analysisRepository.createAnalysis(sequencingReadAnalysis);
    }
  }

  private void updateSampleSetTable(PortalFileMetadata portalFileMetadata){
    for (val sampleEntry : extractSampleEntries(portalFileMetadata)){
      if (!sampleEntrySet.contains(sampleEntry)){
        analysisRepository.addSample(sampleEntry.getAnalysisId(), sampleEntry.getSampleId());
        sampleEntrySet.add(sampleEntry);
      }
    }
  }

  private void updateFileSetTable(Analysis analysis, File file){
      analysisRepository.addFile(analysis.getAnalysisId(), file.getObjectId());
  }

  private Analysis updateAnalysis(PortalFileMetadata portalFileMetadata){
//    val analysis = convertToAnalysis(portalFileMetadata);
//    analysisRepository.createAnalysis(analysis);
//    return analysis;
    return null; //TODO: rtismaHACK
  }

  private File updateFile(PortalFileMetadata portalFileMetadata){
    val file = convertToFile(portalFileMetadata);
    fileRepository.create(file);
    return file;
  }

  public static FileProcessor createFileProcessor(List<PortalFileMetadata> portalFileMetadatas){
    return new FileProcessor(portalFileMetadatas);
  }

  private static List<SampleEntry> extractSampleEntries(PortalFileMetadata portalFileMetadata){
//    val analysis = convertToAnalysis(portalFileMetadata);
//    val analysisId = analysis.getAnalysisId();
//    return portalFileMetadata.getSampleIds().stream()
//        .map(x -> SampleEntry.createSampleEntry(analysisId, x))
//        .collect(toImmutableList());
    return null; //TODO: rtismaHACK
  }

}
