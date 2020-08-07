package bio.overture.song.server;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test", "jwt"})
public class JWTTestConfig {

  @Bean
  @SneakyThrows
  public KeyPair keyPair() {
    val keyGenerator = KeyPairGenerator.getInstance("RSA");
    keyGenerator.initialize(1024);
    return keyGenerator.genKeyPair();
  }
}
