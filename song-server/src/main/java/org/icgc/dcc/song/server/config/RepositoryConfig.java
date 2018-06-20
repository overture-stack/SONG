package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.evicting.EvictingFileRepositoryDecorator;
import org.icgc.dcc.song.server.repository.search.SearchRepository;
import org.icgc.dcc.song.server.utils.Evictor;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Bean
  public Evictor evictor(){
    return new Evictor(entityManager);
  }

  @Bean
  public EvictingFileRepositoryDecorator evictingFileRepositoryDecorator(@Autowired Evictor evictor,
      @Autowired FileRepository fileRepository){
    return new EvictingFileRepositoryDecorator(evictor, fileRepository);
  }

}
