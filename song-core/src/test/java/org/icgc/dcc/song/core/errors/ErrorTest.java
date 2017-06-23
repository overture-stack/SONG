package org.icgc.dcc.song.core.errors;

import lombok.val;
import org.icgc.dcc.song.core.exceptions.SongError;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.extractErrorId;
import static org.icgc.dcc.song.core.utils.Debug.getCallingStackTrace;
import static org.springframework.http.HttpStatus.CONFLICT;

public class ErrorTest {

  @Test
  public void testSongErrorJsonParsing(){
    val expectedError  = new SongError();
    expectedError.setDebugMessage("d1");
    expectedError.setErrorId("something.else");
    expectedError.setHttpStatus(CONFLICT);
    expectedError.setMessage("this message");
    expectedError.setRequestUrl("some url");
    expectedError.setStackTraceElementList(getCallingStackTrace());
    expectedError.setTimestamp(System.currentTimeMillis());

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
