package bio.overture.song.server.service.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Retrieves a statically stored tokens */
@RequiredArgsConstructor
public class StaticTokenService implements TokenService {

  /** Dependencies */
  @NonNull private final String token;

  @Override
  public String getToken() {
    return token;
  }
}
