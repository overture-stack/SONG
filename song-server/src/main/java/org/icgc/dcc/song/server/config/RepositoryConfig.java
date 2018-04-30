package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.repository.search.SearchRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

  @Bean
  public SearchRepository searchRepository(){
    return new SearchRepository();
  }

}
