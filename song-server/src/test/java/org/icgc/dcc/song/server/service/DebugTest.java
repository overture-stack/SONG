package org.icgc.dcc.song.server.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.icgc.dcc.song.server.utils.Debug.getCallingStackTrace;

@Slf4j
public class DebugTest {

  @Test
  public void testCallingStackTrace(){
    val expectedSt = stream(currentThread().getStackTrace()).skip(2).collect(toImmutableList());
    val actualSt = getCallingStackTrace();
    assertThat(actualSt).containsAll(expectedSt);
  }


}
