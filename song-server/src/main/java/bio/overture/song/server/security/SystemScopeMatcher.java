package bio.overture.song.server.security;

import static org.icgc.dcc.common.core.util.Joiners.DOT;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Builder
public class SystemScopeMatcher {

  @NonNull private final String prefix;
  @NonNull private final String suffix;

  public boolean isScopeMatchSystem(@NonNull String tokenScope) {
    return getSystemScope().equals(tokenScope);
  }

  public String getSystemScope() {
    return DOT.join(prefix, suffix);
  }
}
