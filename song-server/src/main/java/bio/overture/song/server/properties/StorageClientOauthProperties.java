package bio.overture.song.server.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("score")
public class StorageClientOauthProperties {
  private String url;
  private final ClientCredentials clientCredentials = new ClientCredentials();

  @Getter
  @Setter
  public static class ClientCredentials {
    private String id;
    private String secret;
    private String tokenUrl;
    private String systemScope;
  }
}
