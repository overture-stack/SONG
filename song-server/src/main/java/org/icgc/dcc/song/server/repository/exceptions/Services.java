package org.icgc.dcc.song.server.repository.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor public enum Services {

  ID_SERVICE("Id");
  @NonNull private final String name;

}
