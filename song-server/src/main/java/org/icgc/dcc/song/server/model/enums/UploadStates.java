package org.icgc.dcc.song.server.model.enums;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@RequiredArgsConstructor
public enum UploadStates {

  CREATED("CREATED"),
  VALIDATED("VALIDATED"),
  VALIDATION_ERROR("VALIDATION_ERROR"),
  UPLOADED("UPLOADED"),
  UPDATED("UPDATED"),
  SAVED("SAVED");

  @Getter
  private final String text;

  public static UploadStates resolveState(@NonNull String uploadState){
      return stream(values())
          .filter(x -> x.getText().equals(uploadState))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(format("The upload state '%s' cannot be resolved",
              uploadState)));
  }

}
