package org.icgc.dcc.song.client.errors;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServerErrors implements ServerError {
  TOKEN_UNAUTHORIZED("token.unauthorized"),
  UNKOWN("unknown");

  @NonNull private final String id;

}
