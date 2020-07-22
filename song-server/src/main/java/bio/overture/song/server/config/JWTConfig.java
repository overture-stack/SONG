package bio.overture.song.server.config;

import bio.overture.song.server.security.CustomResourceServerTokenServices;
import bio.overture.song.server.security.JWTTokenConverter;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JWTConfig {

  private final String publicKeyUrl;
  private final RetryTemplate retryTemplate;
  private final RemoteTokenServices remoteTokenServices;

  @Autowired
  public JWTConfig(
      @NonNull @Value("${auth.jwt.public-key-url}") String publicKeyUrl,
      @NonNull RemoteTokenServices remoteTokenServices,
      @NonNull RetryTemplate retryTemplate){
    this.publicKeyUrl = publicKeyUrl;
    this.retryTemplate = retryTemplate;
    this.remoteTokenServices = remoteTokenServices;
  }

  @Bean
  @Primary
  public CustomResourceServerTokenServices customResourceServerTokenServices(@Autowired JwtTokenStore jwtTokenStore) {
    return new CustomResourceServerTokenServices(
        remoteTokenServices, jwtTokenStore, retryTemplate);
  }

  @Bean
  public JwtTokenStore jwtTokenStore(){
    return new JwtTokenStore(buildJwtTokenConverter());
  }

  private JWTTokenConverter buildJwtTokenConverter() {
    return new JWTTokenConverter(getPublicKey());
  }

  //TODO: rtisma --- ideally, this public key fetching is more dynamic. For instance, if EGO changes its public key,
  // this song server needs to be rebooted, meaning downtime. Would be better if the public key is cached, and
  // when a request fails, try to update cache and if there is a new value, update cache and try again, otherwise
  // error out as normal.
  private String getPublicKey() {
    val rest = new RestTemplate();
    // TODO: [rtisma] add error handling
    ResponseEntity<String> response =
        retryTemplate.execute(x -> rest.getForEntity(publicKeyUrl, String.class));
    return response.getBody();
  }

}
