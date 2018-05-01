package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.repository.search.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@Configuration
public class RepositoryConfig {

  @Autowired EntityManagerFactory entityManagerFactory;

  @Bean
  public EntityManager entityManager(){
    return entityManagerFactory.createEntityManager();
  }

  @Bean
  public SearchRepository searchRepository(EntityManager entityManager){
    return new SearchRepository(entityManager);
  }

}
