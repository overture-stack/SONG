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

package org.icgc.dcc.song.server.service;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

import static java.lang.Boolean.parseBoolean;
import static org.icgc.dcc.common.core.util.Joiners.SLASH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;

@Service
@Slf4j
public class ExistenceService {

  private static final String UPLOAD = "upload";

  @Autowired
  private RetryTemplate retryTemplate;

  @NonNull private final String storageUrl;

  private RestTemplate restTemplate = new RestTemplate();

  public ExistenceService(RetryTemplate retryTemplate, String storageUrl) {
    this.storageUrl = joinUrl(storageUrl, UPLOAD);
    this.retryTemplate = retryTemplate;
  }

  @SneakyThrows
  public boolean isObjectExist(@NonNull String accessToken, @NonNull String objectId) {
    return retryTemplate.execute(retryContext -> {
      val url = new URL(joinUrl(storageUrl, objectId));
      val httpHeaders = new HttpHeaders();
      httpHeaders.set(AUTHORIZATION, accessToken);
      val req = new HttpEntity<>(httpHeaders);
      val resp = restTemplate.exchange(url.toURI(), GET, req, String.class);
      return parseBoolean(resp.getBody());
    });
  }

  public static ExistenceService createExistenceService(RetryTemplate retryTemplate,String baseUrl){
    return new ExistenceService(retryTemplate,baseUrl);
  }

  private static String joinUrl(String ... path){
    return SLASH.join(path);
  }

}
