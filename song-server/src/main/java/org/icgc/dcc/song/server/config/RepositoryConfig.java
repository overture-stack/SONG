package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.repository.search.SearchRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
public class RepositoryConfig {

  @PersistenceContext
  private EntityManager entityManager;


  @Bean
  public SearchRepository searchRepository(){
    return new SearchRepository(entityManager);
  }

}
