package org.icgc.dcc.song.core.exceptions;

import lombok.Getter;
import lombok.val;

import static org.icgc.dcc.song.core.exceptions.SongError.createSongError;

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

  public static ServerException buildServerException(String context, ServerError serverError, String formattedMessage, Object...args ){
    val songError = createSongError(context, serverError, formattedMessage, args);
    return new ServerException(songError);
  }

  public static ServerException buildServerException(Class<?> clazz, ServerError serverError, String formattedMessage, Object...args ){
    val songError = createSongError(clazz, serverError, formattedMessage, args);
    return new ServerException(songError);
  }

  public static void checkServer(boolean expression, String context, ServerError serverError, String formattedMessage,
      Object...args ){
    if (!expression){
      throw buildServerException(context, serverError, formattedMessage, args);
    }
  }

  public static void checkServer(boolean expression, Class<?> clazz, ServerError serverError, String formattedMessage, Object...args ){
      if (!expression){
        throw buildServerException(clazz, serverError, formattedMessage, args);
      }
  }

}
