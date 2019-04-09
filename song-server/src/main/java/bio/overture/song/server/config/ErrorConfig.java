package bio.overture.song.server.config;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorConfig {

  @Bean
  public ErrorProperties myErrorProperties() {
    return new ErrorProperties();
  }
}
