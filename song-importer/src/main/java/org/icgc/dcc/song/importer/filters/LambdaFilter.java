package org.icgc.dcc.song.importer.filters;

import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class LambdaFilter<T,R> implements  Filter<T>{

  private final Filter<R> internalFilter;
  private final Function<T, R> function;

  @Override public boolean isPass(T t) {
    return internalFilter.isPass(function.apply(t));
  }

  public static <T, R> LambdaFilter<T, R> createLambdaFilter(Filter<R> internalFilter, Function<T, R> function) {
    return new LambdaFilter<T, R>(internalFilter, function);
  }

}
