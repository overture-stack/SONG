package org.icgc.dcc.song.server.service;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
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

  /**
   * [JIRA]: DCC-5630 - Add Retry feature for ExistenceService
   * @Autowired
   * private RetryTemplate retryTemplate;
   */

  @NonNull private final String storageUrl;

  public ExistenceService(String storageUrl) {
    this.storageUrl = joinUrl(storageUrl, UPLOAD);
  }

  @SneakyThrows
  public boolean isObjectExist(String accessToken, String objectId) {
//    [JIRA]: DCC-5630 - Add Retry feature for ExistenceService
//    return retryTemplate.execute(new RetryCallback<Boolean, Throwable>() {
//      @Override public Boolean doWithRetry(RetryContext retryContext) throws Throwable {
        val endpointUrl = joinUrl(storageUrl, objectId);
        val url = new URL(endpointUrl);
        val con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer "+accessToken);
        val opt = getReader(con)
            .lines()
            .findFirst();
        return parseBoolean(opt.orElseThrow( () ->  new IllegalStateException("There was no response. Expecting a [true] or [false]")));
//    [JIRA]: DCC-5630 - Add Retry feature for ExistenceService
//      }
//    });
  }

  @SneakyThrows
  public static ExistenceService createExistenceService(String baseUrl){
    return new ExistenceService(baseUrl);
  }

  @SneakyThrows
  private static BufferedReader getReader(HttpURLConnection con){
    return new BufferedReader(new InputStreamReader(con.getInputStream()));
  }

  private static String joinUrl(String ... path){
    return SLASH.join(path);
  }

}
