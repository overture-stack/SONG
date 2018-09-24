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

package bio.overture.song.core.errors;

import bio.overture.song.core.exceptions.ServerErrors;
import bio.overture.song.core.exceptions.SongError;
import lombok.val;
import bio.overture.song.core.utils.JsonUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static bio.overture.song.core.utils.Debug.streamCallingStackTrace;
import static org.springframework.http.HttpStatus.CONFLICT;

public class ErrorTest {

  @Test
  public void testSongErrorJsonParsing(){
    val expectedError  = SongError.builder()
        .debugMessage("d1")
        .errorId("something.else")
        .httpStatusName(CONFLICT.name())
        .httpStatusCode(CONFLICT.value())
        .message("this message")
        .requestUrl("some url")
        .stackTrace(streamCallingStackTrace().map(StackTraceElement::toString).collect(toImmutableList()))
        .timestamp(System.currentTimeMillis())
        .build();

    val actualError = JsonUtils.fromJson(expectedError.toJson(), SongError.class);
    assertThat(actualError).isEqualTo(expectedError);
  }

  @Test
  public void testCorrectErrorId(){
    val expectedErrorId = "unknown.error";
    val actualErrorId = ServerErrors.UNKNOWN_ERROR.getErrorId();
    assertThat(actualErrorId).isEqualTo(expectedErrorId);
  }

  @Test
  public void testIncorrectErrorId(){
    val incorrectEnumName1 = "Unknown_Error";
    val incorrectEnumName2 = "UNKNOWN-ERROR";
    val thrown1 = catchThrowable(() -> ServerErrors.extractErrorId(incorrectEnumName1) );
    val thrown2 = catchThrowable(() -> ServerErrors.extractErrorId(incorrectEnumName2) );
    assertThat(thrown1).isInstanceOf(IllegalArgumentException.class);
    assertThat(thrown2).isInstanceOf(IllegalArgumentException.class);
  }

}
