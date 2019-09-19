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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;

import static java.lang.Boolean.parseBoolean;
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

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplateBuilder()
        .errorHandler(new ServerResponseErrorHandler())
        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
        .additionalInterceptors(new DefaultClientHttpRequestInterceptor(accessToken))
        .build();
  }

  @RequiredArgsConstructor
  public static class DefaultClientHttpRequestInterceptor implements ClientHttpRequestInterceptor{

    @NonNull private final String accessToken;

    @Override public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
        ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
      val h = httpRequest.getHeaders();
      h.remove(CONTENT_TYPE);
      h.setContentType(APPLICATION_JSON_UTF8);
      h.setBearerAuth(accessToken);
      return clientHttpRequestExecution.execute(httpRequest, bytes);
    }
  }

}
