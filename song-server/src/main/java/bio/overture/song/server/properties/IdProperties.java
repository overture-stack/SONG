package bio.overture.song.server.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("id")
public class IdProperties {

  private boolean useLocal;
  private final FederatedProperties federated = new FederatedProperties();

  @Getter
  @Setter
  public static class FederatedProperties {
    private final UriTemplateProperties uriTemplate = new UriTemplateProperties();
    private final AuthProperties auth = new AuthProperties();

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UriTemplateProperties {
      private String donor;
      private String specimen;
      private String sample;
      private String file;
      private final AnalysisTemplateProperties analysis = new AnalysisTemplateProperties();

      @Getter
      @Setter
      public static class AnalysisTemplateProperties {
        private String existence;
        private String generate;
        private String save;
      }
    }

    @Getter
    @Setter
    public static class AuthProperties {
      private final BearerProperties bearer = new BearerProperties();

      @Getter
      @Setter
      public static class BearerProperties {
        private String token;

        private final BearerCredentialsProperties credentials = new BearerCredentialsProperties();

        @Getter
        @Setter
        public static class BearerCredentialsProperties {
          private String url;
          private String clientId;
          private String clientSecret;
        }
      }
    }
  }
}
