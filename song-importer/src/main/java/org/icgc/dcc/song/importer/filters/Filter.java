package org.icgc.dcc.song.importer.filters;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

public interface Filter<T> {

  boolean isPass(T t);

  default List<T> filterToList(Collection<T> collection){
    return filterStream(collection).collect(toImmutableList());
  }

  default Set<T> filterToSet(Collection<T> collection){
    return filterStream(collection).collect(toImmutableSet());
  }

  default Stream<T> filterStream(Collection<T> collection){
    return collection.stream()
        .filter(this::isPass);
  }

}
