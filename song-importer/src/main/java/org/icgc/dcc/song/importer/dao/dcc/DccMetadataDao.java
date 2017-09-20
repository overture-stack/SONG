package org.icgc.dcc.song.importer.dao.dcc;

import org.icgc.dcc.song.importer.model.DccMetadata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;

public interface DccMetadataDao {

  Optional<DccMetadata> findByObjectId(String objectId);

  List<DccMetadata> findByMultiObjectIds(Collection<String> objectIds);

  default List<DccMetadata> findByMultiObjectIds(String ... objectIds){
    return findByMultiObjectIds(newArrayList(objectIds));
  }

}
