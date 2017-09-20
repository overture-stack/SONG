package org.icgc.dcc.song.importer.dao;

import org.icgc.dcc.song.importer.model.DccMetadata;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;

public interface DccMetadataDao {

  Optional<DccMetadata> findByObjectId(String objectId);

  List<DccMetadata> findByMultiObjectIds(List<String> objectIds);

  default List<DccMetadata> findByMultiObjectIds(String ... objectIds){
    return findByMultiObjectIds(newArrayList(objectIds));
  }

}
