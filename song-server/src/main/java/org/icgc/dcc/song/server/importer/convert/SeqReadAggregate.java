package org.icgc.dcc.song.server.importer.convert;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class SeqReadAggregate {

  @NonNull private final String analysisId;
  @NonNull private final String libraryStrategy;
  @NonNull private final String alignmentTool;
  @NonNull private final String referenceGenome;
  @NonNull private final String studyId;
  @NonNull private final String type;
  private final boolean aligned;

}
