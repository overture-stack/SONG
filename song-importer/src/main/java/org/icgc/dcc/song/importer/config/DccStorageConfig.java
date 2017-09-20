package org.icgc.dcc.song.importer.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class DccStorageConfig {

  @Value("${dcc-storage.token}")
  private String token;

  @Value("${dcc-storage.url}")
  private String url;

}
