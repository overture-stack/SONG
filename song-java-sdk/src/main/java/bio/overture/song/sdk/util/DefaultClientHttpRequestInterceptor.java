package bio.overture.song.sdk.util;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@RequiredArgsConstructor
public class DefaultClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  @NonNull private final String accessToken;

  @Override
  public ClientHttpResponse intercept(
      HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution)
      throws IOException {
    val h = httpRequest.getHeaders();
    h.remove(CONTENT_TYPE);
    h.remove(ACCEPT);
    h.setAccept(newArrayList(APPLICATION_JSON_UTF8));
    h.setContentType(APPLICATION_JSON_UTF8);
    h.setBearerAuth(accessToken);
    return clientHttpRequestExecution.execute(httpRequest, bytes);
  }
}
