package bio.overture.song.server.config;

import bio.overture.song.server.properties.StorageClientOauthProperties;
import bio.overture.song.server.service.StorageService;
import bio.overture.song.server.service.ValidationService;
import java.util.List;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("score-client-cred")
public class StorageClientOauthConfig {

  private final StorageClientOauthProperties storageClientOauthProperties;
  private final RetryTemplate retryTemplate;
  private final ValidationService validationService;

  @Autowired
  public StorageClientOauthConfig(
      StorageClientOauthProperties storageClientOauthProperties,
      RetryTemplate retryTemplate,
      ValidationService validationService) {
    this.storageClientOauthProperties = storageClientOauthProperties;
    this.retryTemplate = retryTemplate;
    this.validationService = validationService;
  }

  @Primary
  @Bean
  public StorageService storageService() {
    return StorageService.builder()
        .restTemplate(oauth2ClientCredentialsRestTempalate())
        .retryTemplate(retryTemplate)
        .storageUrl(storageClientOauthProperties.getUrl())
        .validationService(validationService)
        .build();
  }

  private RestTemplate oauth2ClientCredentialsRestTempalate() {
    val clientCredentials = storageClientOauthProperties.getClientCredentials();

    val resource = new ClientCredentialsResourceDetails();

    resource.setAccessTokenUri(clientCredentials.getTokenUrl());
    resource.setClientId(clientCredentials.getId());
    resource.setClientSecret(clientCredentials.getSecret());
    resource.setScope(List.of(clientCredentials.getSystemScope()));

    val clientContext = new DefaultOAuth2ClientContext();

    return new OAuth2RestTemplate(resource, clientContext);
  }
}
