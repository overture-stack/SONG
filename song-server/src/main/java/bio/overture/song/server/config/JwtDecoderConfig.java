package bio.overture.song.server.config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Slf4j
@Getter
@Setter
@Profile("secure")
@Configuration
@ConfigurationProperties("spring.security.oauth2.resourceserver.jwt")
public class JwtDecoderConfig {

  private Resource publicKeyLocation;

  private String jwkSetUri;

  @Bean
  @Primary
  public JwtDecoder jwtDecoder() {
    try {
      if (isPublicKeyConfigured()) {
        return NimbusJwtDecoder.withPublicKey(loadRsaPublicKey()).build();
      } else if (isJwkSetUriConfigured()) {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
      } else {
        throw new IllegalStateException(
            "Neither public-key-location nor jwk-set-uri is configured.");
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to configure JwtDecoder", e);
    }
  }

  private boolean isPublicKeyConfigured() {
    return publicKeyLocation != null && publicKeyLocation.exists();
  }

  private boolean isJwkSetUriConfigured() {
    return jwkSetUri != null && !jwkSetUri.isBlank();
  }

  private RSAPublicKey loadRsaPublicKey() throws Exception {
    try (InputStream input = publicKeyLocation.getInputStream()) {
      String key =
          new String(input.readAllBytes(), StandardCharsets.UTF_8)
              .replace("-----BEGIN PUBLIC KEY-----", "")
              .replace("-----END PUBLIC KEY-----", "")
              .replaceAll("\\s+", "");
      byte[] decoded = Base64.getDecoder().decode(key);

      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PublicKey publicKey = keyFactory.generatePublic(keySpec);

      if (!(publicKey instanceof RSAPublicKey)) {
        throw new IllegalArgumentException("The provided key is not an RSA public key");
      }

      return (RSAPublicKey) publicKey;
    }
  }
}
