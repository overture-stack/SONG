package bio.overture.song.server.utils;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

import bio.overture.song.server.service.auth.TokenService;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@RequiredArgsConstructor
public class CustomRequestInterceptor implements ClientHttpRequestInterceptor {

  @NonNull private final TokenService tokenService;

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    val h = request.getHeaders();
    h.remove(CONTENT_TYPE);
    h.remove(ACCEPT);
    h.setAccept(List.of(APPLICATION_JSON_UTF8));
    h.setContentType(APPLICATION_JSON_UTF8);
    h.setBearerAuth(tokenService.getToken());
    return execution.execute(request, body);
  }
}
