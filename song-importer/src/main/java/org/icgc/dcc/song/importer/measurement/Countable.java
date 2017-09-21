package org.icgc.dcc.song.importer.measurement;

public interface Countable<T> {

  void incr();

  void incr(T amount);

  default <A> A streamIncr(A object){
    this.incr();
    return object;
  }

  default <A> A streamIncr(A object, T amount){
    this.incr(amount);
    return object;
  }

  void reset();

  T getCount();

}
