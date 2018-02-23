package org.icgc.dcc.song.server.utils;

import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerError;
import org.icgc.dcc.song.core.exceptions.ServerException;

import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ErrorTesting {

  public static <T> void assertSongError(Supplier<T> supplier, ServerError expectedServerError){
    assertSongError(supplier, expectedServerError, null);
  }

  public static <T> void assertSongError(Supplier<T> supplier, ServerError
      expectedServerError, String formattedFailMessage, Object...objects){
    val thrown = catchThrowable(supplier::get);

    val assertion = assertThat(thrown);
    if (!isNull(formattedFailMessage)){
      assertion.describedAs(format(formattedFailMessage, objects));
    }
    assertion.isInstanceOf(ServerException.class);

    val songError = ((ServerException)thrown).getSongError();
    assertThat(songError.getErrorId()).isEqualTo(expectedServerError.getErrorId());
    assertThat(songError.getHttpStatusCode()).isEqualTo(expectedServerError.getHttpStatus().value());
  }

}
