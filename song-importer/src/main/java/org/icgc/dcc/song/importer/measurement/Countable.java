package org.icgc.dcc.song.importer.measurement;

import java.util.Collection;

public interface Countable<T> {

  void incr();

  void incr(T amount);

  default <A> A streamCount(A object){
    this.incr();
    return object;
  }

  <C extends Collection<?>> C streamCollectionCount(C objects);

  void reset();

  T getCount();

}
