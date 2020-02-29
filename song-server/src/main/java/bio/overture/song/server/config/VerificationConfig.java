package bio.overture.song.server.config;

import bio.overture.song.server.service.RestVerificationService;
import bio.overture.song.server.service.VerificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Data
public class VerificationConfig {
  private List<String> verifierUrls = new ArrayList<>();

  @Bean
  RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  List<VerificationService> getVerifiers(RestTemplate restTemplate) {
    return verifierUrls.stream()
        .map(s -> new RestVerificationService(restTemplate, s))
        .collect(Collectors.toUnmodifiableList());
  }
}
