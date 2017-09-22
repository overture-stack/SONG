package org.icgc.dcc.song.server.model.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum InfoTypes {
  STUDY("Study"),
  DONOR("Donor"),
  SPECIMEN("Specimen"),
  SAMPLE("Sample"),
  FILE("File"),
  ANALYSIS("Analysis"),
  SEQUENCING_READ("SequencingRead"),
  VARIANT_CALL("VariantCall");

  private final String text;

  InfoTypes() {
    this.text = name();
  }

  public String toString(){
  return text;
 }

}
