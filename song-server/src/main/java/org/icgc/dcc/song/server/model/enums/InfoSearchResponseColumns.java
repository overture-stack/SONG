package org.icgc.dcc.song.server.model.enums;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum InfoSearchResponseColumns {
  ANALYSIS_ID("analysis_id"),
  INFO("info");

  @NonNull private final String text;

  public String toString(){
    return text;
  }

}
