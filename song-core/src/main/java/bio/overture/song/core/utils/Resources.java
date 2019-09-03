package bio.overture.song.server.utils;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public class Resources {

  public static String getResourceContent(String resourceFilename) throws IOException {
    try (val inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilename)) {
      if (isNull(inputStream)) {
        throw new IOException(
            format("The classpath resource '%s' does not exist", resourceFilename));
      }
      return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(joining("\n"));
    }
  }
}
