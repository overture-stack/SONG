package org.icgc.dcc.song.server.importer;

import com.google.common.collect.Maps;
import lombok.val;
import org.icgc.dcc.song.server.importer.model.PortalDonorMetadata;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.icgc.dcc.song.server.repository.UploadRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.song.server.importer.convert.Converters.convertToAnalysis;
import static org.icgc.dcc.song.server.importer.convert.Converters.convertToFile;

public class FileProcessor implements Runnable {

  private final List<PortalFileMetadata> portalFileMetadatas;
  private final Map<String, PortalDonorMetadata> donorMap;
  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private StudyRepository studyRepository;
  @Autowired private UploadRepository uploadRepository;

  private FileProcessor(List<PortalFileMetadata> portalFileMetadatas,
      Map<String, PortalDonorMetadata> donorMap) {
    this.portalFileMetadatas = portalFileMetadatas;
    this.donorMap = donorMap;
  }

  @Override
  public void run() {
    for (val fileMetadata : portalFileMetadatas){
      val file = convertToFile(fileMetadata);
      val analysis = convertToAnalysis(fileMetadata);

      fileRepository.create(file);
      analysisRepository.createAnalysis(analysis);
    }
  }

  public static FileProcessor createFileProcessor(List<PortalFileMetadata> portalFileMetadatas,
      Map<String, PortalDonorMetadata> donorMap) {
    return new FileProcessor(portalFileMetadatas, donorMap);
  }

  public static FileProcessor createFileProcessor(List<PortalFileMetadata> portalFileMetadatas,
      List<PortalDonorMetadata> portalDonorMetadatas) {
    return createFileProcessor(portalFileMetadatas, createDonorMap(portalDonorMetadatas));
  }

  private static Map<String , PortalDonorMetadata> createDonorMap(List<PortalDonorMetadata> portalDonorMetadatas){
    val map = Maps.<String, PortalDonorMetadata>newHashMap();
    for (val donor : portalDonorMetadatas){
      checkState(map.containsKey(donor.getId()), "The donorId [%s] already exists. The input PortaldonorMetadatalist "
          + "should have unique entries", donor.getId());
      map.put(donor.getId(), donor);
    }
    return map;
  }

}
