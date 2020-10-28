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
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@NoArgsConstructor
@Configuration
@Profile("!storageClientCred")
public class StorageDefaultConfig {

  @Autowired private RetryTemplate retryTemplate;

  @Autowired private ValidationService validationService;

  @Value("${score.url}")
  private String storageUrl;

  @Value("#{'Bearer '.concat('${score.accessToken}')}")
  private String scoreAuthorizationHeader;

  @Bean
  public StorageService storageService() {
    return StorageService.builder()
        .restTemplate(tokenInjectedRestTemplate())
        .retryTemplate(retryTemplate)
        .storageUrl(storageUrl)
        .validationService(validationService)
        .build();
  }

  private RestTemplate tokenInjectedRestTemplate() {
    val restTemplate = new RestTemplate();

    ClientHttpRequestInterceptor accessTokenAuthIntercept =
        (request, body, clientHttpRequestExecution) -> {
          request.getHeaders().add(AUTHORIZATION, scoreAuthorizationHeader);
          return clientHttpRequestExecution.execute(request, body);
        };

    restTemplate.getInterceptors().add(accessTokenAuthIntercept);

    return restTemplate;
  }
}
