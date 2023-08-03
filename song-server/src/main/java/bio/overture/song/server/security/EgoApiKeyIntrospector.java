package bio.overture.song.server.security;

import bio.overture.song.core.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Slf4j
@AllArgsConstructor
public class EgoApiKeyIntrospector implements OpaqueTokenIntrospector {

    private String introspectionUri;
    private String clientId;
    private String clientSecret;

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        try {
            // Add token to introspectionUri
            val uriWithToken =
                    UriComponentsBuilder.fromHttpUrl(introspectionUri)
                            .queryParam("apiKey", token)
                            .build()
                            .toUri();

            // Get response from Ego
            val template = new RestTemplate();
            val response =
                    template.postForEntity(
                            uriWithToken, new HttpEntity<Void>(null, getBasicAuthHeader()), JsonNode.class);
            
            // Ensure response was OK
            if ((response.getStatusCode() != HttpStatus.OK
                    && response.getStatusCode() != HttpStatus.MULTI_STATUS
                    && response.getStatusCode() != HttpStatus.UNAUTHORIZED)
                    || !response.hasBody()) {
                throw new OAuth2IntrospectionException("Bad Response from Ego Server");
            }

            val responseBody = response.getBody();

            val isValid = validateIntrospectResponse(response.getStatusCode(), responseBody);
            if (!isValid) {
                throw new BadOpaqueTokenException("ApiKey is revoked or expired.");
            }

            // Ego ApiKey check is successful. Build authenticated principal and return.
            return convertResponseToPrincipal(responseBody);
        } catch (Exception e) {
          if(e instanceof HttpClientErrorException.Unauthorized){
            // throw 401 HTTP error code
            throw new BadCredentialsException(e.getMessage(), e);
          } else {
            // throw 500 HTTP error code
            throw new OAuth2IntrospectionException(e.getMessage());
          }
        }
    }

    private HttpHeaders getBasicAuthHeader() {
        val headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        return headers;
    }

    private boolean validateIntrospectResponse(HttpStatus status, JsonNode response) {
        if (response.has("error")) {
            log.debug("Check Token response includes an error: {}", response.has("error"));
            return false;
        }
        if (status != HttpStatus.OK && status != HttpStatus.MULTI_STATUS) {
            log.debug(
                    "Check Token response is unauthorized but does not list the error. Rejecting token.");
            return false;
        }
    /* TODO: joneubank 2022-06-10 this should be checking for expiry and active=true instead of just the presence of
       an error field, but at the moment the Ego check_api_token endpoint either returns 401+error, or it is active
       (Ego version 5.3.0)
    */
        return true;
    }

    private OAuth2AuthenticatedPrincipal convertResponseToPrincipal(JsonNode responseJson) {
        val response = JsonUtils.convertValue(responseJson, EgoApiKeyIntrospectResponse.class);

        Collection<GrantedAuthority> authorities = new ArrayList();
        Map<String, Object> claims = new HashMap<>();

        if (!response.getScope().isEmpty()) {
            List<String> scopes = Collections.unmodifiableList(response.getScope());
            claims.put("scope", scopes);
            val var5 = scopes.iterator();

            while (var5.hasNext()) {
                String scope = (String) var5.next();
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
            }
        }

        return new OAuth2IntrospectionAuthenticatedPrincipal(claims, authorities);
    }
}
