package org.icgc.dcc.song.server.model.enums;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;
import static java.util.Arrays.stream;

@RequiredArgsConstructor
public enum AccessTypes {
  OPEN("open"),
  CONTROLLED("controlled");

  @NonNull private final String text;

  public static AccessTypes resolveAccessType(@NonNull String accessType){
    return stream(values())
        .filter(x -> x.toString().equals(accessType))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(format("The access type '%s' cannot be resolved", accessType)));
  }

  public String toString(){
    return text;
  }

}
