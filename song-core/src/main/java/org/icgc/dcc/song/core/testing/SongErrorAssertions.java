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

package org.icgc.dcc.song.core.testing;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerError;
import org.icgc.dcc.song.core.exceptions.ServerException;

import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class SongErrorAssertions {
  private static final Object EMPTY_OBJECT = new Object();

  public static void assertSongError(Runnable runnable, ServerError expectedServerError){
    assertSongError(runnable, expectedServerError, null);
  }

  public static <T> void assertSongError(Supplier<T> supplier, ServerError expectedServerError){
    assertSongError(supplier, expectedServerError, null);
  }

  public static void assertSongError(@NonNull Runnable runnable, ServerError expectedServerError,
      String formattedFailMessage, Object...objects){
    assertSongError(() -> {
      runnable.run();
      return EMPTY_OBJECT;
    }, expectedServerError, formattedFailMessage, objects);
  }

  public static <T> void assertSongError(@NonNull Supplier<T> supplier,
      @NonNull ServerError expectedServerError, String formattedFailMessage, Object...objects){
    val thrown = catchThrowable(supplier::get);

    val assertion = assertThat(thrown);
    if (!isNull(formattedFailMessage)){
      assertion.describedAs(format(formattedFailMessage, objects));
    }
    assertion.as(format("a %s should have been thrown", ServerException.class.getSimpleName())).isInstanceOf(ServerException.class);

    val songError = ((ServerException)thrown).getSongError();
    assertThat(songError.getErrorId()).isEqualTo(expectedServerError.getErrorId());
    assertThat(songError.getHttpStatusCode()).isEqualTo(expectedServerError.getHttpStatus().value());
  }

}
