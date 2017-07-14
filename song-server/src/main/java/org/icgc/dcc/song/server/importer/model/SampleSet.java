package org.icgc.dcc.song.server.importer.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class SampleSet {

  @NonNull private final String analysisId;
  @NonNull private final String sampleId;

  public static SampleSet createSampleEntry(String analysisId, String sampleId) {
    return new SampleSet(analysisId, sampleId);
  }

}
