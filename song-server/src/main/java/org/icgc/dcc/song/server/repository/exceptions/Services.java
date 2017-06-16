package org.icgc.dcc.song.server.repository.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor public enum Services {

  ID("Id");
  @NonNull private final String name;

}
