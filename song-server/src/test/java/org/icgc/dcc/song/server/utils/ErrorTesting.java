package org.icgc.dcc.song.server.utils;

import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerError;
import org.icgc.dcc.song.core.exceptions.ServerException;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ErrorTesting {

  public static <T> void assertSongError(Supplier<T> supplier, ServerError expectedServerError){
    val thrown = catchThrowable(supplier::get);
    assertThat(thrown).isInstanceOf(ServerException.class);
    val songError = ((ServerException)thrown).getSongError();
    assertThat(songError.getErrorId()).isEqualTo(expectedServerError.getErrorId());
    assertThat(songError.getHttpStatusCode()).isEqualTo(expectedServerError.getHttpStatus().value());
  }

}
