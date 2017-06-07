package org.icgc.dcc.sodalite.server.config;

import lombok.NoArgsConstructor;
import org.icgc.dcc.sodalite.server.service.ExistenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.icgc.dcc.sodalite.server.service.ExistenceService.createExistenceService;

@NoArgsConstructor
@Configuration
public class ExistenceConfig {

  private static final String STORAGE_API = "https://storage.cancercollaboratory.org";

  @Bean
  public ExistenceService existenceService(){
    return createExistenceService(STORAGE_API);
  }

}
