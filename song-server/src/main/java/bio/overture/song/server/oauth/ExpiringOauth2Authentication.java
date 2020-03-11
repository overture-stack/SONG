package bio.overture.song.server.oauth;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.hibernate.validator.internal.metadata.aggregated.rule.OverridingMethodMustNotAlterParameterConstraints;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.servlet.tags.EvalTag;

import javax.swing.*;

@Getter
public class ExpiringOauth2Authentication extends OAuth2Authentication {
  private final int expiry; // expiry time of the authentication token in seconds

  public ExpiringOauth2Authentication(OAuth2Request storedRequest,
    Authentication userAuthentication, int expiry) {
    super(storedRequest, userAuthentication);
    this.expiry = expiry;
  }

  public static ExpiringOauth2Authentication from(OAuth2Authentication existing, int expiry) {
    return new ExpiringOauth2Authentication(existing.getOAuth2Request(), existing.getUserAuthentication(), expiry);
  }
}
