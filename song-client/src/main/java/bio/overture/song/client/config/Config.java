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
package bio.overture.song.client.config;

import bio.overture.song.client.errors.ServerResponseErrorHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "client")
public class Config {

  private String serverUrl;

  private String studyId;

  private String programName;

  private String accessToken;

  private String debug;

  private final RetryConfig retry = new RetryConfig();

  public boolean isDebug() {
    return parseBoolean(debug);
  }

  /**
   * This webresource takes care of the serverUrl and authorization headers, meaning they dont need
   * to be called explicitly in the code
   */
  @Bean
  public RestTemplate restTemplate() {
    val restTemplate = new RestTemplate(clientHttpRequestFactory());
    restTemplate.setErrorHandler(new ServerResponseErrorHandler());
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(serverUrl));
    return restTemplate;
  }

  /** Request Factory that contains security headers */
  private HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
    val factory = new HttpComponentsClientHttpRequestFactory();
    val clientBuilder = HttpClients.custom();
    configureDefaultHeaders(clientBuilder);
    val client = clientBuilder.build();
    factory.setHttpClient(client);
    return factory;
  }

  private void configureDefaultHeaders(HttpClientBuilder client) {
    client.setDefaultHeaders(
        newArrayList(
            new BasicHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8.toString()),
            new BasicHeader(AUTHORIZATION, format("Bearer %s", accessToken))));
  }
}
