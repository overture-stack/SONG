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
import bio.overture.song.server.repository.AnalysisRepository;
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

  /**
   * Constants
   */
  private static final UUID NAMESPACE_UUID =
      UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");

  /**
   * Dependencies
   */
  private final IdProperties idProperties;
  private final AnalysisRepository analysisRepository;
  private final RetryTemplate retryTemplate;

  @Autowired
  public IdConfig(@NonNull IdProperties idProperties,
      @NonNull AnalysisRepository analysisRepository,
      @NonNull RetryTemplate retryTemplate) {
    this.idProperties = idProperties;
    this.analysisRepository = analysisRepository;
    this.retryTemplate = retryTemplate;
  }

  @Bean
  public NameBasedGenerator nameBasedGenerator() {
    return createNameBasedGenerator();
  }


  @Bean
  public IdService idService(@Autowired NameBasedGenerator nameBasedGenerator){
    if (idProperties.isUseLocal()){
      log.info("Loading LOCAL mode for IdService");
      return new LocalIdService(nameBasedGenerator, analysisRepository);
    } else{
      log.info("Loading FEDERATED mode for IdService");
      val uriResolver = createUriResolver(idProperties.getFederated().getUriTemplate());
      val restClient = new RestClient(restTemplate(), retryTemplate);
      return new FederatedIdService(restClient, uriResolver);
    }
  }

  @SneakyThrows
  public static NameBasedGenerator createNameBasedGenerator() {
    return Generators.nameBasedGenerator(NAMESPACE_UUID, MessageDigest.getInstance("SHA-1"));
  }

  private RestTemplate restTemplate(){
    val rest = new RestTemplate();
    if (isStaticAuthMode()){
      log.info("Static auth mode enabled for IdService");
      rest.getInterceptors().add(staticAuthInterceptor());
    } else if(isDynamicAuthMode()) {
      log.info("Dynamic auth mode enabled for IdService");
      val message = "Dynamic auth mode has not been implemented yet. This is just a placeholder";
      log.error(message);
      throw new NotImplementedException(message);
    } else{
      log.info("No auth mode enabled for IdService");

    }
    return rest;
  }

  private ClientHttpRequestInterceptor staticAuthInterceptor(){
    val authService = new StaticTokenService(idProperties.getFederated().getAuth().getBearer().getToken());
    return new CustomRequestInterceptor(authService);
  }

  private boolean isStaticAuthMode(){
    return !isBlank(idProperties.getFederated().getAuth().getBearer().getToken());
  }

  private boolean isDynamicAuthMode(){
    val authCredentials = idProperties.getFederated().getAuth().getBearer().getCredentials();
    val isClientIdDefined = !isBlank(authCredentials.getClientId());
    val isClientSecretDefined = !isBlank(authCredentials.getClientSecret());
    checkState(isClientIdDefined == isClientSecretDefined, "Both clientId and clientSecret must be defined, or undefined");
    return isClientIdDefined;
  }

}
