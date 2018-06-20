package org.icgc.dcc.song.server.utils;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.Session;

import javax.persistence.EntityManager;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class HibernateUtils {

  public static Session getSession(@NonNull EntityManager entityManager){
    return entityManager.unwrap(Session.class);
  }

}
