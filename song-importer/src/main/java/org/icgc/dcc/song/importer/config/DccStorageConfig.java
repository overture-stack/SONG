package org.icgc.dcc.song.importer.config;

import lombok.Getter;
import org.icgc.dcc.song.importer.storage.SimpleDccStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.icgc.dcc.song.importer.storage.SimpleDccStorageClient.createSimpleDccStorageClient;

@Configuration
@Getter
public class DccStorageConfig {

  @Value("${dcc-storage.token}")
  private String token;

  @Value("${dcc-storage.url}")
  private String url;

  @Value("${dcc-storage.persist}")
  private boolean persist;

  @Value("${dcc-storage.bypassMd5Check}")
  private boolean bypassMd5Check;

  @Value("${dcc-storage.outputDir}")
  private String outputDir;

  @Value("${dcc-storage.forceDownload}")
  private boolean forceDownload;

  @Bean
  public SimpleDccStorageClient simpleDccStorageClient(DccStorageConfig dccStorageConfig){
    return createSimpleDccStorageClient(dccStorageConfig);
  }

}
