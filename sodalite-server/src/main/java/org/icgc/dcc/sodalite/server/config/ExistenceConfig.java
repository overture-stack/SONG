package org.icgc.dcc.sodalite.server.config;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@NoArgsConstructor
@Configuration
@Profile("secure")
public class ExistenceConfig {

  @Bean
  public String storageUrl(){
    return "https://storage.cancercollaboratory.org";
  }

}
