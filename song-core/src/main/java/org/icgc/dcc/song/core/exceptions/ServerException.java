package org.icgc.dcc.song.core.exceptions;

import lombok.Getter;

@Getter
public class ServerException extends RuntimeException {

  private final SongError songError;

  public ServerException(SongError songError) {
    super(songError.getMessage());
    this.songError = songError;
  }

  public ServerException(SongError songError,Throwable cause){
    super(songError.getMessage(), cause);
    this.songError = songError;
  }

}
