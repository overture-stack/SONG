package org.icgc.dcc.song.importer.download.fetcher;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.importer.convert.DccMetadataConverter;
import org.icgc.dcc.song.importer.dao.dcc.DccMetadataDao;
import org.icgc.dcc.song.importer.dao.dcc.impl.DccMetadataDbDao;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.storage.SimpleDccStorageClient;
import org.icgc.dcc.song.server.model.entity.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.song.importer.convert.FileConverter.convertToFile;
import static org.icgc.dcc.song.importer.measurement.CounterMonitor.newMonitor;
import static org.icgc.dcc.song.importer.storage.SimpleDccStorageClient.calcMd5Sum;

@Component
public class DccMetadataFetcher {

  @Autowired private final DccMetadataDao dccMetadataDao;
  @Autowired private final SimpleDccStorageClient simpleDccStorageClient;

  public DccMetadataFetcher(@NonNull DccMetadataDbDao dccMetadataDao, @NonNull SimpleDccStorageClient
      simpleDccStorageClient) {
    this.dccMetadataDao = dccMetadataDao;
    this.simpleDccStorageClient = simpleDccStorageClient;
  }

  public List<File> fetchDccMetadataFiles(List<PortalFileMetadata> portalFileMetadatas){
    val objectIdMap = portalFileMetadatas.stream()
        .collect(groupingBy(DccMetadataConverter::getId));
    val dccMetadatas = dccMetadataDao.findByMultiObjectIds(objectIdMap.keySet());

    val list = ImmutableList.<File>builder();
    val counterMonitor = newMonitor("dccMetadataFiles",100);
    counterMonitor.start();
    for (val dccMetadata :dccMetadatas){
      val objectId = dccMetadata.getId();
      val portalFileMetadatasForObjectId = objectIdMap.get(objectId);
      val file = simpleDccStorageClient.getFile(objectId,"N/A", dccMetadata.getFileName());
      portalFileMetadatasForObjectId.stream()
          .map(x -> buildFile(dccMetadata, file, x))
          .forEach(list::add);
      counterMonitor.incr(portalFileMetadatasForObjectId.size());
    }
    counterMonitor.stop();
    counterMonitor.displaySummary();
    return list.build();
  }

  @SneakyThrows
  private File buildFile(DccMetadata dccMetadata, java.io.File file, PortalFileMetadata portalFileMetadata){
    val songFile = convertToFile(dccMetadata,file, portalFileMetadata);
    val md5sum = calcMd5Sum(file.toPath());
    val songFileSize = Files.size(file.toPath());
    songFile.setFileSize(songFileSize);
    songFile.setFileMd5sum(md5sum);
    return songFile;
  }

}
