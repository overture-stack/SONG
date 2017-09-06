package org.icgc.dcc.song.server.model.enums;

import static java.util.Arrays.stream;

public enum AnalysisStates {
  PUBLISHED,
  UNPUBLISHED,
  SUPPRESSED;

  public String toString(){
    return this.name();
  }

  public static String[] toStringArray(){
    return stream(values())
        .map(Enum::name)
        .toArray(String[]::new);
  }

}
