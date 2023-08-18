package bio.overture.song.server.security;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@Slf4j
@Builder
public class KeycloakAuthorizationService {

  private static final String UMA_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:uma-ticket";
  private static final String UMA_AUDIENCE = "song";
  private static final String UMA_RESPONSE_MODE = "permissions";

  @NonNull private String introspectionUri;

  public List<KeycloakPermission> fetchAuthorizationGrants(String accessToken){
    // Add token to introspectionUri
    val uriWithToken =
        UriComponentsBuilder.fromHttpUrl(introspectionUri)
            .build()
            .toUri();

    HttpEntity<MultiValueMap<String, String>> request =
        new HttpEntity<>(getUmaParams(), getBearerAuthHeader(accessToken));

    // Get response from Keycloak
    val template = new RestTemplate();
    template.setErrorHandler(new RestTemplateResponseErrorHandler());
    val response =
        template.postForEntity(
            uriWithToken, request, KeycloakPermission[].class);

    // Ensure response was OK
    if ((response.getStatusCode() != HttpStatus.OK
        && response.getStatusCode() != HttpStatus.MULTI_STATUS
        && response.getStatusCode() != HttpStatus.UNAUTHORIZED)
        || !response.hasBody()) {
      throw new OAuth2IntrospectionException("Bad Response from Keycloak Server");
    }

    val isValid = validateIntrospectResponse(response.getStatusCode());
    if (!isValid) {
      throw new BadOpaqueTokenException("ApiKey is revoked or expired.");
    }

    return List.of(response.getBody());
  }

  private MultiValueMap<String, String> getUmaParams(){
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("grant_type", UMA_GRANT_TYPE);
    map.add("audience", UMA_AUDIENCE);
    map.add("response_mode", UMA_RESPONSE_MODE);
    return map;
  }

  private HttpHeaders getBearerAuthHeader(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBearerAuth(token);
    return headers;
  }

  private boolean validateIntrospectResponse(HttpStatus status) {
    if (status != HttpStatus.OK && status != HttpStatus.MULTI_STATUS) {
      log.debug(
          "Check Token response is unauthorized but does not list the error. Rejecting token.");
      return false;
    }
    return true;
  }

  private static class RestTemplateResponseErrorHandler
      implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
        throws IOException {

      return (
          httpResponse.getStatusCode().series() == CLIENT_ERROR
              || httpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse)
        throws IOException {

      if (httpResponse.getStatusCode().series() == CLIENT_ERROR) {
        // throw 401 HTTP error code
        throw new BadCredentialsException(httpResponse.getStatusText());
      } else {
        // throw 500 HTTP error code
        throw new OAuth2IntrospectionException(httpResponse.getStatusText());
      }
    }
  }

}
