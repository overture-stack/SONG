/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.server.config;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import bio.overture.song.server.service.StorageService;
import bio.overture.song.server.service.ValidationService;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

@NoArgsConstructor
@Configuration
public class StorageConfig {

  @Autowired private RetryTemplate retryTemplate;

  @Autowired private ValidationService validationService;

  @Value("${score.url}")
  private String storageUrl;

  @Value("#{'Bearer '.concat('${score.accessToken}')}")
  private String scoreAuthorizationHeader;

  // TODO encapsulate with a POJO maybe?
  @Value("${score.clientCredentials.enabled}")
  private Boolean clientCredentialsEnabled;

  @Value("${score.clientCredentials.id}")
  private String clientCredentialsId;

  @Value("${score.clientCredentials.secret}")
  private String clientCredentialsSecret;

  @Value("${score.clientCredentials.tokenUrl}")
  private String clientCredentialsTokenUrl;

  @Bean
  public StorageService storageService() {
    RestTemplate restTemplate;
    if (clientCredentialsEnabled) {
      restTemplate = oauthClientCredentialTemplate();
    } else {
      restTemplate = accessTokenInjectedTemplate();
    }
    return StorageService.builder()
        .restTemplate(restTemplate)
        .retryTemplate(retryTemplate)
        .storageUrl(storageUrl)
        .validationService(validationService)
        .build();
  }

  private RestTemplate accessTokenInjectedTemplate() {
    val restTemplate = new RestTemplate();

    ClientHttpRequestInterceptor accessTokenAuthIntercept =
        (request, body, clientHttpRequestExecution) -> {
          request.getHeaders().add(AUTHORIZATION, scoreAuthorizationHeader);
          return clientHttpRequestExecution.execute(request, body);
        };

    restTemplate.getInterceptors().add(accessTokenAuthIntercept);

    return restTemplate;
  }

  private RestTemplate oauthClientCredentialTemplate() {
    ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();

    resource.setAccessTokenUri(clientCredentialsTokenUrl);
    resource.setClientId(clientCredentialsId);
    resource.setClientSecret(clientCredentialsSecret);

    DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext();

    return new OAuth2RestTemplate(resource, clientContext);
  }
}
