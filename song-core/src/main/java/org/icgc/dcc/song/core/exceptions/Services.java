package org.icgc.dcc.song.core.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor public enum Services {

  ID_SERVICE("Id"),
  UPLOAD_SERVICE("Upload");
  @NonNull private final String name;

}
