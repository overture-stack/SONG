package org.icgc.dcc.song.server.service;

import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

import static java.lang.Boolean.parseBoolean;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.Joiners.SLASH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;

@Service
@Slf4j
public class ExistenceService {

  private static final String UPLOAD = "upload";

  private static final int DEFAULT_TIMEOUT = 2;

  @Autowired
  private RetryTemplate retryTemplate;

  @NonNull private final String storageUrl;

  @Setter(PRIVATE)
  private int timeoutMs;
  private RestTemplate restTemplate = new RestTemplate();

  public ExistenceService(RetryTemplate retryTemplate, String storageUrl) {
    this.storageUrl = joinUrl(storageUrl, UPLOAD);
    this.retryTemplate = retryTemplate;
    setTimeoutMs(DEFAULT_TIMEOUT);
  }

  @SneakyThrows
  public boolean isObjectExist(@NonNull String accessToken, @NonNull String objectId) {
    return retryTemplate.execute(retryContext -> {
      val url = new URL(joinUrl(storageUrl, objectId));
      val httpHeaders = new HttpHeaders();
      httpHeaders.set(AUTHORIZATION, accessToken);
      val req = new HttpEntity<>(httpHeaders);
      val rf =(SimpleClientHttpRequestFactory)restTemplate.getRequestFactory();
      rf.setConnectTimeout(timeoutMs);
      rf.setReadTimeout(timeoutMs);
      val resp = restTemplate.exchange(url.toURI(), GET, req, String.class);
      return parseBoolean(resp.getBody());
    });
  }


  public static ExistenceService createExistenceService(RetryTemplate retryTemplate, String storageUrl, int timeout){
    val e = createExistenceService(retryTemplate, storageUrl);
    e.setTimeoutMs(timeout);
    return e;
  }

  public static ExistenceService createExistenceService(RetryTemplate retryTemplate,String baseUrl){
    return new ExistenceService(retryTemplate,baseUrl);
  }

  private static String joinUrl(String ... path){
    return SLASH.join(path);
  }

}
