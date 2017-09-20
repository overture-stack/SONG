package org.icgc.dcc.song.importer.dao.dcc.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.importer.dao.dcc.DccMetadataDao;
import org.icgc.dcc.song.importer.model.DccMetadata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

@RequiredArgsConstructor
public class DccMetadataCachedDao implements DccMetadataDao {

  @NonNull private final Collection<DccMetadata> data;

  @Override
  public Optional<DccMetadata> findByObjectId(String objectId) {
    return data.stream()
        .filter(x -> x.getId().equals(objectId))
        .findFirst();
  }

  @Override
  public List<DccMetadata> findByMultiObjectIds(Collection<String> objectIds) {
    val objectIdSet = newHashSet(objectIds);
    return data.stream()
        .filter(x -> objectIdSet.contains(x.getId()))
        .collect(toImmutableList());
  }

  public static DccMetadataCachedDao createDccMetadataCachedDao(Collection<DccMetadata> data) {
    return new DccMetadataCachedDao(data);
  }

}
