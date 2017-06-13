package org.icgc.dcc.song.server.config;

import lombok.NoArgsConstructor;
import org.icgc.dcc.song.server.service.ExistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

import static org.icgc.dcc.song.server.service.ExistenceService.createExistenceService;

@NoArgsConstructor
@Configuration
public class ExistenceConfig {

  @Autowired
  private RetryTemplate retryTemplate;

  @Value("${dcc-storage.url}")
  private String storageUrl;

  @Bean
  public ExistenceService existenceService(){
    return createExistenceService(retryTemplate,storageUrl);
  }

}
