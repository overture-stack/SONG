package org.icgc.dcc.song.importer.dao.dcc;

import org.icgc.dcc.song.importer.model.DccMetadata;

import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public interface DccMetadataDao {

  Optional<DccMetadata> findByObjectId(String objectId);

  Set<DccMetadata> findByMultiObjectIds(Set<String> objectIds);

  default Set<DccMetadata> findByMultiObjectIds(String ... objectIds){
    return findByMultiObjectIds(newHashSet(objectIds));
  }

}
