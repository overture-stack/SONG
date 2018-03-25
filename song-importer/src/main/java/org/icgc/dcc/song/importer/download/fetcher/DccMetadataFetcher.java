/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.icgc.dcc.song.importer.download.fetcher;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.importer.convert.DccMetadataConverter;
import org.icgc.dcc.song.importer.dao.dcc.DccMetadataDao;
import org.icgc.dcc.song.importer.model.DccMetadata;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.storage.SimpleDccStorageClient;
import org.icgc.dcc.song.server.model.entity.File;

import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.convert.FileConverter.convertToFile;
import static org.icgc.dcc.song.importer.measurement.CounterMonitor.newMonitor;
import static org.icgc.dcc.song.importer.storage.SimpleDccStorageClient.calcMd5Sum;

public class DccMetadataFetcher {

  private final SimpleDccStorageClient simpleDccStorageClient;
  private final DccMetadataDao dccMetadataDao;

  public DccMetadataFetcher(@NonNull DccMetadataDao dccMetadataDao, @NonNull SimpleDccStorageClient
      simpleDccStorageClient) {
    this.dccMetadataDao = dccMetadataDao;
    this.simpleDccStorageClient = simpleDccStorageClient;
  }

  public Set<File> fetchDccMetadataFiles(List<PortalFileMetadata> portalFileMetadatas){
    val objectIdMap = groupDccMetadataIds(portalFileMetadatas);
    val dccMetadatas = dccMetadataDao.findByMultiObjectIds(objectIdMap.keySet());

    val counterMonitor = newMonitor("dccMetadataFiles",1000);
    counterMonitor.start();
    val set = dccMetadatas.stream()
        .map(x -> processDccMetadata(x, objectIdMap))
        .map(counterMonitor::streamCollectionCount)
        .flatMap(Collection::stream)
        .collect(toImmutableSet());
    counterMonitor.stop();
    counterMonitor.displaySummary();
    return set;
  }

  private Set<File> processDccMetadata(DccMetadata dccMetadata, Map<String, List<PortalFileMetadata>> objectIdMap){
    val objectId = dccMetadata.getId();
    val portalFileMetadatasForObjectId = objectIdMap.get(objectId);
    val file = simpleDccStorageClient.getFile(objectId,"N/A", dccMetadata.getFileName());
    return portalFileMetadatasForObjectId.stream()
        .map(x -> buildFile(dccMetadata, file, x))
        .collect(toImmutableSet());
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

  private static Map<String, List<PortalFileMetadata>> groupDccMetadataIds(List<PortalFileMetadata>
      portalFileMetadatas){
    return portalFileMetadatas.stream()
        .collect(groupingBy(DccMetadataConverter::getId));
  }

  public static DccMetadataFetcher createDccMetadataFetcher(@NonNull DccMetadataDao dccMetadataDao,
      @NonNull SimpleDccStorageClient simpleDccStorageClient) {
    return new DccMetadataFetcher(dccMetadataDao, simpleDccStorageClient);
  }
}
