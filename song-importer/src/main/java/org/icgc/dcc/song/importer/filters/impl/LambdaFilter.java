/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
