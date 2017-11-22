package org.icgc.dcc.song.importer.dao.dcc.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.dao.dcc.DccMetadataDao;
import org.icgc.dcc.song.importer.model.DccMetadata;

import java.util.Optional;
import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

@RequiredArgsConstructor
public class DccMetadataMemoryDao implements DccMetadataDao {

  @NonNull private final Set<DccMetadata> data;

  @Override
  public Optional<DccMetadata> findByObjectId(String objectId) {
    return data.stream()
        .filter(x -> x.getId().equals(objectId))
        .findFirst();
  }

  @Override
  public Set<DccMetadata> findByMultiObjectIds(Set<String> objectIds) {
    return data.stream()
        .filter(x -> objectIds.contains(x.getId()))
        .collect(toImmutableSet());
  }

  public static DccMetadataMemoryDao createDccMetadataMemoryDao(Set<DccMetadata> data) {
    return new DccMetadataMemoryDao(data);
  }

}
