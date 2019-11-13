package bio.overture.song.server.service.id;

import bio.overture.song.server.service.auth.TokenService;
import bio.overture.song.server.utils.CustomRequestInterceptor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTest {

  @Mock private TokenService tokenService;

  @Test
  @SneakyThrows
  public void tokenInjection_mockRequest_success(){
    // Setup tokenService behaviour
    when(tokenService.getToken()).thenReturn("mytoken");

    // Create interceptor
    val interceptor = new CustomRequestInterceptor(tokenService);

    // Create a mock http request
    val httpRequest = new MockClientHttpRequest();
    httpRequest.setMethod(HttpMethod.GET);
    httpRequest.setURI(null);
    httpRequest.setResponse(null);

    // Assert the Authorization header field does not exist
    assertFalse(httpRequest.getHeaders().containsKey(AUTHORIZATION));

    // Execute interception of the httpRequest
    interceptor.intercept(httpRequest, null, new ClientHttpRequestExecution(){
      @Override public ClientHttpResponse execute(HttpRequest request, byte[] body) throws IOException {
        return null;
      }
    });

    // Verify getToken method is called
    verify(tokenService, times(1)).getToken();

    // Assert the header now contains the Authorization field
    assertTrue(httpRequest.getHeaders().containsKey(AUTHORIZATION));
    val auths = httpRequest.getHeaders().get(AUTHORIZATION);
    assertTrue(auths.size() == 1);
    assertEquals(auths.get(0), "Bearer mytoken");
  }

}
