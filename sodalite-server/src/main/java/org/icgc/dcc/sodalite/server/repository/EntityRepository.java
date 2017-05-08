package org.icgc.dcc.sodalite.server.repository;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.Entity;

public interface EntityRepository<T extends Entity> {

  String add(String parentId, T entity);

  String update(T entity);

  int delete(String id);

  String deleteByParentId(String id);

  T getById(String id);

  List<T> findByParentId(String parentId);

  List<T> find(Map<String, String> searchParams);

  List<String> getIds(String parentId);
}
