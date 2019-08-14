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

package bio.overture.song.core.testing;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.core.exceptions.ServerException;
import lombok.NonNull;
import lombok.val;

import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.catchThrowable;

public class SongErrorAssertions {
  private static final Object EMPTY_OBJECT = new Object();

  // NOTE: for some reason, jvm11 does not like the below method name "assertSongError".
  //       If the below method name was "assertSongError", although this is basic
  //       method overloading, the compiler errors out with
  //       "incompatible types: inference variable T has incompatible bounds"
  public static void assertSongErrorRunnable(Runnable runnable, ServerError expectedServerError){
    assertSongErrorRunnable(runnable, expectedServerError, null);
  }

  public static <T> void assertSongError(Supplier<T> supplier, ServerError expectedServerError){
    assertSongError(supplier, expectedServerError, null);
  }

  public static void assertSongErrorRunnable(@NonNull Runnable runnable, ServerError expectedServerError,
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
    assertEquals(songError.getErrorId(),expectedServerError.getErrorId());
    assertEquals(songError.getHttpStatusCode(),expectedServerError.getHttpStatus().value());
  }

}
