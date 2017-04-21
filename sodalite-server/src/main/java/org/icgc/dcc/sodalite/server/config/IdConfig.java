package org.icgc.dcc.sodalite.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.earnstone.id.Generator;

import lombok.Data;


@Configuration
@Data
@ConfigurationProperties(prefix = "id")
public class IdConfig {

  private long serverInstance;
  private long workerInstance;
  
  @Bean
  public Generator idGenerator() {
  	return new Generator(serverInstance, workerInstance);
  }

}
