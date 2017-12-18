package org.icgc.dcc.song.client.benchmark.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Stat {

  @NonNull private final String studyId;
  private final long numFiles;
  private final long totalSize;
  private final float totalTimeMs;
  private final float speed;

}
