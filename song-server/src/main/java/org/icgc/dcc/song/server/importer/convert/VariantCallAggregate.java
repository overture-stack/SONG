package org.icgc.dcc.song.server.importer.convert;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class VariantCallAggregate {

  @NonNull private final String analysisId;
  @NonNull private final String variantCallingTool;
  @NonNull private final String matchedNormalSampleSubmitterId;
  @NonNull private final String studyId;
  @NonNull private final String type;

}
