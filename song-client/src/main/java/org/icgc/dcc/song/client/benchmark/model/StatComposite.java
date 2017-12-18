package org.icgc.dcc.song.client.benchmark.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class StatComposite {

  @NonNull private final String studyId;
  @NonNull private final Stat uploadStat;
  @NonNull private final Stat statusStat;
  @NonNull private final Stat saveStat;

}
