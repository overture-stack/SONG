package bio.overture.song.server.security;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.Joiners.DOT;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class SystemScopeMatcher {

  @NonNull
  private final String systemScope;

  @Builder
  public SystemScopeMatcher(@NonNull String prefix, @NonNull String suffix) {
    this(DOT.join(prefix, suffix));
  }

  public boolean isScopeMatchSystem(@NonNull String tokenScope) {
    return getSystemScope().equals(tokenScope);
  }

}
