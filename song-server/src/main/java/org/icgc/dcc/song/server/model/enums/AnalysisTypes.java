package org.icgc.dcc.song.server.model.enums;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.common.core.util.stream.Streams;

import static java.lang.String.format;

@RequiredArgsConstructor
public enum AnalysisTypes {
  SEQUENCING_READ("sequencingRead"),
  VARIANT_CALL("variantCall");

  private final String text;

  public String toString(){
    return text;
  }

  public static AnalysisTypes resolveAnalysisType(@NonNull String analysisType){
    return Streams.stream(values())
        .filter(x -> x.toString().equals(analysisType))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(format("The analysis type '%s' cannot be resolved", analysisType)));
  }


}
