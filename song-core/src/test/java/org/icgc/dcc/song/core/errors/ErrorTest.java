package org.icgc.dcc.song.core.errors;

import lombok.val;
import org.icgc.dcc.song.core.exceptions.SongError;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.extractErrorId;
import static org.icgc.dcc.song.core.utils.Debug.streamCallingStackTrace;
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
    val actualErrorId = UNKNOWN_ERROR.getErrorId();
    assertThat(actualErrorId).isEqualTo(expectedErrorId);
  }

  @Test
  public void testIncorrectErrorId(){
    val incorrectEnumName1 = "Unknown_Error";
    val incorrectEnumName2 = "UNKNOWN-ERROR";
    val thrown1 = catchThrowable(() -> extractErrorId(incorrectEnumName1) );
    val thrown2 = catchThrowable(() -> extractErrorId(incorrectEnumName2) );
    assertThat(thrown1).isInstanceOf(IllegalArgumentException.class);
    assertThat(thrown2).isInstanceOf(IllegalArgumentException.class);
  }

}
