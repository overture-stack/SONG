package org.icgc.dcc.song.importer.resolvers;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;
import static java.util.Arrays.stream;

@RequiredArgsConstructor
public enum AccessTypes {
  OPEN("open"),
  CONTROLLED("controlled");

  @NonNull @Getter private final String text;

  public static AccessTypes resolve(@NonNull String accessType){
    return stream(values())
        .filter(x -> x.getText().equals(accessType))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(format("The access type '%s' cannot be resolved", accessType)));
  }

}
