package org.icgc.dcc.song.core.utils;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@NoArgsConstructor(access = PRIVATE)
public class Debug {

  /**
   * Gets the stacktrace List of the calling function
   */
  public static List<StackTraceElement> getCallingStackTrace(){
    return stream(currentThread().getStackTrace())
        .skip(2)
        .collect(toImmutableList());
  }

  public static Stream<StackTraceElement> streamCallingStackTrace(){
    return stream(currentThread().getStackTrace())
        .skip(2);
  }

}
