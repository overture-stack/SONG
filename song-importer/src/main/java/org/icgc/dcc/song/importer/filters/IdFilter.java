package org.icgc.dcc.song.importer.filters;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Set;

@RequiredArgsConstructor
public class IdFilter implements Filter<String> {

  private final Set<String> ids;
  private final boolean isGoodIds;

  @Override public boolean isPass(String id) {
    val isContains = ids.contains(id);
    return isGoodIds == isContains;
  }

  public static IdFilter createIdFilter(Set<String> ids, boolean isGoodIds) {
    return new IdFilter(ids, isGoodIds);
  }

}
