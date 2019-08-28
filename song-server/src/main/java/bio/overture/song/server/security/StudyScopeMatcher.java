package bio.overture.song.server.security;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PRIVATE;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class StudyScopeMatcher {

  @NonNull
  @Getter(value = NONE)
  private final String fullPrefix;

  @NonNull
  @Getter(value = NONE)
  private final String fullSuffix;

  @Builder
  public StudyScopeMatcher(
      @NonNull String prefix,
      @NonNull String firstDelimiter,
      @NonNull String secondDelimiter,
      @NonNull String suffix) {
    this(prefix+firstDelimiter, secondDelimiter+suffix);
  }

  public boolean isScopeMatchStudy(@NonNull String tokenScope, @NonNull String studyId) {
    return getStudyScope(studyId).equals(tokenScope);
  }

  public String getStudyScope(@NonNull String studyId) {
    return fullPrefix+studyId+fullSuffix;
  }

}
