package bio.overture.song.server.model.legacy;

import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;

public class EgoIsDownException extends InvalidTokenException {
  public EgoIsDownException(String s) {
    super(s);
  }
}
