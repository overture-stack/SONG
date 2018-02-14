package org.icgc.dcc.song.importer.filters.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.importer.filters.Filter;

import java.util.function.Function;

@RequiredArgsConstructor
public class LambdaFilter<T,R> implements Filter<T> {

  @NonNull private final Filter<R> internalFilter;
  @NonNull private final Function<T, R> function;

  @Override public boolean isPass(T t) {
    return internalFilter.isPass(function.apply(t));
  }

  public static <T, R> LambdaFilter<T, R> createLambdaFilter(Filter<R> internalFilter, Function<T, R> function) {
    return new LambdaFilter<T, R>(internalFilter, function);
  }

}
