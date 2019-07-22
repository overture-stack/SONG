package bio.overture.song.server.security;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static org.icgc.dcc.common.core.util.Joiners.DOT;

/**
 * Provides methods of validating scopes at the study-level and system-level
 */
@Slf4j
@Value
@Builder
public class ScopeValidator {

  @NonNull private final String scopePrefix;
  @NonNull private final String scopeSuffix;

  public boolean verifyOneOfStudyScope(@NonNull Set<String> grantedScopes, @NonNull final String studyId) {
    return grantedScopes.stream().anyMatch(s -> isGrantedForStudy(s, studyId));
  }

  public boolean verifyOneOfSystemScope(@NonNull Set<String> grantedScopes) {
    return grantedScopes.stream().anyMatch(this::isGrantedForSystem);
  }

  private boolean isGrantedForStudy(@NonNull String tokenScope, @NonNull String studyId) {
    log.info("Checking study scope '{}', server's scopePrefix='{}', studyId '{}', scopeSuffix='{}'",
        tokenScope, getScopePrefix(), studyId, getScopeSuffix());
    return getSystemScope().equals(tokenScope) || getStudyScope(studyId).equals(tokenScope); //short-circuit
  }

  private boolean isGrantedForSystem(@NonNull String tokenScope) {
    log.info("Checking system scope '{}', server's scopePrefix='{}', scopeSuffix='{}'",
        tokenScope, getScopePrefix(), getScopeSuffix());
    return getSystemScope().equals(tokenScope);
  }

  private String getStudyScope(String studyId){
    return DOT.join(scopePrefix, studyId, scopeSuffix);
  }

  private String getSystemScope(){
    return DOT.join(scopePrefix,scopeSuffix);
  }

}
