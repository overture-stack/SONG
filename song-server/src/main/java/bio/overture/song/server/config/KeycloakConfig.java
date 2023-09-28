package bio.overture.song.server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
@Getter
public class KeycloakConfig {

  @Value("${auth.server.clientID}")
  private String uma_audience;

  @Value("${auth.server.keycloak.host}")
  private String host;

  @Value("${auth.server.keycloak.realm}")
  private String realm;

  @Value("${auth.server.keycloak.enabled:false}")
  private boolean enabled;

  private static final String UMA_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:uma-ticket";
  private static final String UMA_RESPONSE_MODE = "permissions";

  public URI permissionUrl(){
    return UriComponentsBuilder.fromHttpUrl(host)
        .path("realms")
        .path(realm)
        .path("protocol/openid-connect/token")
        .build()
        .toUri();
  }

  public MultiValueMap<String, String> getUmaParams(){
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("grant_type", UMA_GRANT_TYPE);
    map.add("audience", uma_audience);
    map.add("response_mode", UMA_RESPONSE_MODE);
    return map;
  }
}
