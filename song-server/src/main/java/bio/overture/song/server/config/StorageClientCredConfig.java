package bio.overture.song.server.config;

import bio.overture.song.server.service.StorageService;
import bio.overture.song.server.service.ValidationService;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

@NoArgsConstructor
@Configuration
@Profile("storageClientCred")
public class StorageClientCredConfig {

  @Autowired private RetryTemplate retryTemplate;

  @Autowired private ValidationService validationService;

  @Value("${score.url}")
  private String storageUrl;

  @Value("${score.clientCredentials.id}")
  private String clientId;

  @Value("${score.clientCredentials.secret}")
  private String clientSecret;

  @Value("${score.clientCredentials.tokenUrl}")
  private String tokenUrl;

  @Value("${score.clientCredentials.systemScope}")
  private String systemScope;

  @Primary
  @Bean
  public StorageService storageService() {
    return StorageService.builder()
        .restTemplate(oauth2ClientCredentialsRestTempalate())
        .retryTemplate(retryTemplate)
        .storageUrl(storageUrl)
        .validationService(validationService)
        .build();
  }

  private RestTemplate oauth2ClientCredentialsRestTempalate() {
    ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();

    resource.setAccessTokenUri(tokenUrl);
    resource.setClientId(clientId);
    resource.setClientSecret(clientSecret);
    resource.setScope(List.of(systemScope));

    DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext();

    return new OAuth2RestTemplate(resource, clientContext);
  }
}
