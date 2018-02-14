package org.icgc.dcc.song.importer.filters.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.importer.filters.Filter;

import java.util.Set;

@RequiredArgsConstructor
public class IdFilter implements Filter<String> {

  @NonNull private final Set<String> ids;
  @NonNull private final boolean isGoodIds;

  @Override public boolean isPass(String id) {
    val isContains = ids.contains(id);
    return isGoodIds == isContains;
  }

  public static IdFilter createIdFilter(Set<String> ids, boolean isGoodIds) {
    return new IdFilter(ids, isGoodIds);
  }

}
