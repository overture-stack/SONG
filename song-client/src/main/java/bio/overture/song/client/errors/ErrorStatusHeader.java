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

package bio.overture.song.client.errors;

import static bio.overture.song.core.utils.Debug.generateHeader;
import static java.lang.String.format;

import bio.overture.song.client.config.CustomRestClientConfig;
import bio.overture.song.core.exceptions.SongError;
import lombok.val;

public class ErrorStatusHeader {

  private static final String SONG_SERVER_ERROR_TITLE = "SONG SERVER ERROR";
  private static final String SONG_CLIENT_ERROR_TITLE = "SONG CLIENT ERROR";
  private static final int NUM_SYMBOLS = 60;
  private static final String SING_SYMBOL = " 🎵 ";
  private static final String HEADER_SYMBOL = "*";
  private static final String SONG_SERVER_ERROR_HEADER =
      generateHeader(SONG_SERVER_ERROR_TITLE, NUM_SYMBOLS, HEADER_SYMBOL);
  private static final String SONG_CLIENT_ERROR_HEADER =
      generateHeader(SONG_CLIENT_ERROR_TITLE, NUM_SYMBOLS, HEADER_SYMBOL);

  private final boolean debugEnabled;

  public ErrorStatusHeader(CustomRestClientConfig config) {
    this.debugEnabled = config.isDebug();
  }

  public String getSongServerErrorOutput(SongError songError) {
    return debugEnabled
        ? format("%s\n%s", SONG_SERVER_ERROR_HEADER, songError.toPrettyJson())
        : songError.toString();
  }

  public String getSongClientErrorOutput(SongError songError) {
    return debugEnabled
        ? format("%s\n%s", SONG_CLIENT_ERROR_HEADER, songError.toPrettyJson())
        : songError.toString();
  }

  public String createMessage(String format, Object... args) {
    val message = format(format, args);
    return debugEnabled ? format("%s\n%s", SONG_CLIENT_ERROR_HEADER, message) : message;
  }
}
