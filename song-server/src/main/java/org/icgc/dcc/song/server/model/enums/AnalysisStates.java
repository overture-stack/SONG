package org.icgc.dcc.song.server.model.enums;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public enum AnalysisStates {
  PUBLISHED,
  UNPUBLISHED,
  SUPPRESSED;

  public String toString(){
    return this.name();
  }

  public static List<String> toList(){
    return stream(values())
        .map(Enum::name)
        .collect(Collectors.toList());
  }

}
