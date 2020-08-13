package bio.overture.song.server.config;

import bio.overture.song.server.security.CustomResourceServerTokenServices;
import bio.overture.song.server.security.DefaultPublicKeyFetcher;
import bio.overture.song.server.security.JWTTokenConverter;
import bio.overture.song.server.security.PublicKeyFetcher;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

@Profile("jwt")
@Configuration
public class JWTConfig {

  private final String publicKeyUrl;
  private final RetryTemplate retryTemplate;
  private final RemoteTokenServices remoteTokenServices;

  @Autowired
  public JWTConfig(
      @NonNull @Value("${auth.jwt.public-key-url}") String publicKeyUrl,
      @NonNull RemoteTokenServices remoteTokenServices,
      @NonNull RetryTemplate retryTemplate) {
    this.publicKeyUrl = publicKeyUrl;
    this.retryTemplate = retryTemplate;
    this.remoteTokenServices = remoteTokenServices;
  }

  @Bean
  @Primary
  public CustomResourceServerTokenServices customResourceServerTokenServices(
      @Autowired PublicKeyFetcher publicKeyFetcher) {
    return new CustomResourceServerTokenServices(
        remoteTokenServices, buildJwtTokenStore(publicKeyFetcher), retryTemplate);
  }

  private JwtTokenStore buildJwtTokenStore(@Autowired PublicKeyFetcher publicKeyFetcher) {
    return new JwtTokenStore(jwtTokenConverter(publicKeyFetcher));
  }

  public JWTTokenConverter jwtTokenConverter(@Autowired PublicKeyFetcher publicKeyFetcher) {
    return new JWTTokenConverter(publicKeyFetcher.getPublicKey());
  }

  @Bean
  @Profile("!test")
  public PublicKeyFetcher publicKeyFetcher() {
    return new DefaultPublicKeyFetcher(publicKeyUrl, new RestTemplate(), retryTemplate);
  }
}
