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

import bio.overture.song.server.properties.IdProperties;
import bio.overture.song.server.properties.IdProperties.FederatedProperties.AuthProperties.BearerProperties;
import bio.overture.song.server.service.auth.StaticTokenService;
import bio.overture.song.server.service.id.FederatedIdService;
import bio.overture.song.server.service.id.IdService;
import bio.overture.song.server.service.id.LocalIdService;
import bio.overture.song.server.service.id.RestClient;
import bio.overture.song.server.utils.CustomRequestInterceptor;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isBlank;
import static bio.overture.song.server.service.id.UriResolver.createUriResolver;

@Slf4j
@Configuration
public class IdConfig {

  /** Constants */
  private static final UUID NAMESPACE_UUID =
      UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");

  /** Dependencies */
  private final IdProperties idProperties;

  private final RetryTemplate retryTemplate;

  @Autowired
  public IdConfig(@NonNull IdProperties idProperties, @NonNull RetryTemplate retryTemplate) {
    this.idProperties = idProperties;
    this.retryTemplate = retryTemplate;
  }


  @Bean
  public IdService idService() {
    val localIdService = new LocalIdService(createNameBasedGenerator());
    if (idProperties.isUseLocal()) {
      log.info("Loading LOCAL mode for IdService");
      return localIdService;
    } else {
      log.info("Loading FEDERATED mode for IdService");
      val uriResolver = createUriResolver(idProperties.getFederated().getUriTemplate());
      val restTemplate = restTemplate(idProperties.getFederated().getAuth().getBearer());
      val restClient = new RestClient(restTemplate, retryTemplate);
      return new FederatedIdService(restClient, localIdService, uriResolver);
    }
  }

  @SneakyThrows
  public static NameBasedGenerator createNameBasedGenerator() {
    return Generators.nameBasedGenerator(NAMESPACE_UUID, MessageDigest.getInstance("SHA-1"));
  }

  private static RestTemplate restTemplate(BearerProperties bearerProperties) {
    val rest = new RestTemplate();
    if (isStaticAuthMode(bearerProperties)) {
      log.info("Static auth mode enabled for IdService");
      rest.getInterceptors().add(staticAuthInterceptor(bearerProperties));
      // Placeholder for issue SONG-491
    } else if (isDynamicAuthMode(bearerProperties)) {
      log.info("Dynamic auth mode enabled for IdService");
      val message = "Dynamic auth mode has not been implemented yet. This is just a placeholder";
      log.error(message);
      throw new NotImplementedException(message);
    } else {
      log.info("No auth mode enabled for IdService");
    }
    return rest;
  }

  private static ClientHttpRequestInterceptor staticAuthInterceptor(
      BearerProperties bearerProperties) {
    val authService = new StaticTokenService(bearerProperties.getToken());
    return new CustomRequestInterceptor(authService);
  }

  private static boolean isStaticAuthMode(BearerProperties bearerProperties) {
    return !isBlank(bearerProperties.getToken());
  }

  // Placeholder for issue SONG-491
  private static boolean isDynamicAuthMode(BearerProperties bearerProperties) {
    val authCredentials = bearerProperties.getCredentials();
    val isUrlDefined = !isBlank(authCredentials.getUrl());
    val isClientIdDefined = !isBlank(authCredentials.getClientId());
    val isClientSecretDefined = !isBlank(authCredentials.getClientSecret());
    checkState(
        isClientIdDefined == isClientSecretDefined && isClientIdDefined == isUrlDefined,
        "url, clientId and clientSecret must ALL be defined, or undefined");
    return isClientIdDefined;
  }
}
