package org.icgc.dcc.song.server.model.enums;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

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

  public static InfoTypes resolveInfoType(@NonNull String infoTypeValue){
    return stream(values())
        .filter(x -> x.toString().equals(infoTypeValue))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(format("The info type '%s' cannot be resolved",
            infoTypeValue)));
  }

}
