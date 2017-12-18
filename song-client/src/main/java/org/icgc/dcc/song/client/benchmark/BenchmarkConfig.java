package org.icgc.dcc.song.client.benchmark;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.nio.file.Path;
import java.util.Set;

@Value
@Builder
public class BenchmarkConfig {

  @NonNull private final String serverUrl;
  @NonNull private final String accessToken;
  @NonNull private final Path inputDataDir;
  private final boolean allowStudyCreation;
  private final boolean ignoreIdCollisions;
  @NonNull @Singular Set<String> excludeStudies;
  @NonNull @Singular Set<String> includeStudies;

}
