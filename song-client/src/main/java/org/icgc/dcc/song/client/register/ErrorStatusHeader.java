package org.icgc.dcc.song.client.register;

import org.icgc.dcc.song.client.config.Config;
import org.icgc.dcc.song.core.exceptions.SongError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static org.icgc.dcc.song.core.utils.Debug.generateHeader;

@Component
public class ErrorStatusHeader {
  
  private static final String SONG_SERVER_ERROR_TITLE = "SONG SERVER ERROR";
  private static final String SONG_CLIENT_ERROR_TITLE = "SONG CLIENT ERROR";
  private static final int NUM_SYMBOLS = 60;
  private static final String SING_SYMBOL = " 🎵 ";
  private static final String HEADER_SYMBOL = "*";
  private static final String SONG_SERVER_ERROR_HEADER = generateHeader(SONG_SERVER_ERROR_TITLE, NUM_SYMBOLS, HEADER_SYMBOL);
  private static final String SONG_CLIENT_ERROR_HEADER = generateHeader(SONG_CLIENT_ERROR_TITLE, NUM_SYMBOLS, HEADER_SYMBOL);

  private final boolean debugEnabled;

  @Autowired
  public ErrorStatusHeader(Config config){
    this.debugEnabled = config.isDebug();
  }

  public String getSongServerErrorOutput(SongError songError){
    return debugEnabled ? format("%s\n%s", SONG_SERVER_ERROR_HEADER,songError.toPrettyJson()) : songError.toString();
  }

  public String getSongClientErrorOutput(SongError songError){
    return debugEnabled ? format("%s\n%s", SONG_CLIENT_ERROR_HEADER,songError.toPrettyJson()) : songError.toString();
  }

}
