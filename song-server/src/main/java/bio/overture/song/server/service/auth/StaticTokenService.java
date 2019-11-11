package bio.overture.song.server.service.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StaticTokenService implements TokenService {

  @NonNull private final String token;

  @Override
  public String getToken() {
    return token;
  }

}
