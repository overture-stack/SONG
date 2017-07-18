package org.icgc.dcc.song.importer.filters;

import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class IdFilter implements Filter<String> {

  private final Set<String> badIds;

  @Override public boolean isPass(String id) {
    return !badIds.contains(id);
  }

  public static IdFilter createIdFilter(Set<String> badIds) {
    return new IdFilter(badIds);
  }

}
