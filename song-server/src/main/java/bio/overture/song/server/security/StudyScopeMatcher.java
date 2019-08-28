package bio.overture.song.server.security;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Builder
public class StudyScopeMatcher {

  @NonNull private final String prefix;
  @NonNull private final String firstDelimiter;
  @NonNull private final String secondDelimiter;
  @NonNull private final String suffix;

  public boolean isScopeMatchStudy(@NonNull String tokenScope, @NonNull String studyId) {
    return getStudyScope(studyId).equals(tokenScope);
  }

  public String getStudyScope(String studyId) {
    return prefix + firstDelimiter + studyId + secondDelimiter + suffix;
  }
}
