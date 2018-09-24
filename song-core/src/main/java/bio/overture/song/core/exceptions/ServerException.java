/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.core.exceptions;

import lombok.Getter;
import lombok.val;

import static bio.overture.song.core.exceptions.SongError.createSongError;

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
