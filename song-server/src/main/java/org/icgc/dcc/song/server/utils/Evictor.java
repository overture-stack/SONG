package org.icgc.dcc.song.server.utils;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

public class Evictor {

  private final EntityManager entityManager;

  private Session session;

  public Evictor( @Autowired EntityManager entityManager) {
    this.entityManager  = entityManager;
  }

  public Session getSession(){
    return HibernateUtils.getSession(entityManager);
  }

  public <E> List<E> evictList(List<E> inputList){
    return inputList
        .stream()
        .peek(getSession()::evict)
        .collect(toImmutableList());
  }

  public <E> E evictObject(E e){
    getSession().evict(e);
    return e;
  }

  public <E> Optional<E> evictOptional(Optional<E> e){
    return e.map(this::evictObject);
  }

  public <E> Page<E> evictPage(Page<E> e){
    return e.map(this::evictObject);
  }

}
