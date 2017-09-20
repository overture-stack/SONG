package org.icgc.dcc.song.importer.download.fetcher;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.icgc.dcc.song.importer.convert.DccMetadataConverter;
import org.icgc.dcc.song.importer.dao.dcc.DccMetadataDao;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.model.entity.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.song.importer.convert.FileConverter.convertToFile;

@Component
public class DccMetadataFetcher {

  private final DccMetadataDao dao;
  //rtisma placeholder for realthing     private final CollaboratoryStorage collaboratoryStorage;

  @Autowired
  public DccMetadataFetcher(DccMetadataDao dccMetadataDao) {
    this.dao = dccMetadataDao;
  }

  public List<File> fetchDccMetadataFiles(List<PortalFileMetadata> portalFileMetadatas){
    val objectIdMap = portalFileMetadatas.stream()
        .collect(groupingBy(DccMetadataConverter::getId));
    val dccMetadatas = dao.findByMultiObjectIds(objectIdMap.keySet());
    val file = new java.io.File(""); //TODO: HACK rtisma temp placeholder

    val list = ImmutableList.<File>builder();
    for (val dccMetadata :dccMetadatas){
      val objectId = dccMetadata.getId();
      val portalFileMetadatasForObjectId = objectIdMap.get(objectId);
      portalFileMetadatasForObjectId.stream()
          .map(x -> convertToFile(dccMetadata, file, x))
          .forEach(list::add);
    }
    return list.build();
  }


}
