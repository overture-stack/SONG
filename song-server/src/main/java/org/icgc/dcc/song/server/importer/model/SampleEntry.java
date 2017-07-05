package org.icgc.dcc.song.server.importer.model;

import lombok.Value;

@Value
public class SampleEntry {

  private final String analysisId;
  private final String sampleId;

  public static SampleEntry createSampleEntry(String analysisId, String sampleId) {
    return new SampleEntry(analysisId, sampleId);
  }

}
