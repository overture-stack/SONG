package org.icgc.dcc.song.server.service;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Boolean.parseBoolean;
import static org.icgc.dcc.common.core.util.Joiners.SLASH;

@Service
public class ExistenceService {

  private static final String UPLOAD = "upload";

  @Autowired
  private RetryTemplate retryTemplate;

  @NonNull private final String storageUrl;

  public ExistenceService(RetryTemplate retryTemplate, String storageUrl) {
    this.storageUrl = joinUrl(storageUrl, UPLOAD);
    this.retryTemplate = retryTemplate;
  }

  @SneakyThrows
  public boolean isObjectExist(@NonNull String accessToken, @NonNull String objectId) {
    return retryTemplate.execute(retryContext -> {
        val endpointUrl = joinUrl(storageUrl, objectId);
        val url = new URL(endpointUrl);
        val con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", accessToken);
        val opt = getReader(con)
            .lines()
            .findFirst();
        return parseBoolean(opt.orElseThrow( () ->  new IllegalStateException("There was no response. Expecting a [true] or [false]")));
    });
  }

  @SneakyThrows
  public static ExistenceService createExistenceService(RetryTemplate retryTemplate,String baseUrl){
    return new ExistenceService(retryTemplate,baseUrl);
  }

  @SneakyThrows
  private static BufferedReader getReader(HttpURLConnection con){
    return new BufferedReader(new InputStreamReader(con.getInputStream()));
  }

  private static String joinUrl(String ... path){
    return SLASH.join(path);
  }

}
