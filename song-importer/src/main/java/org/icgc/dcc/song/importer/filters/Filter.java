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

package org.icgc.dcc.song.importer.filters;

import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

public interface Filter<T> {

  boolean isPass(T t);

  default boolean isFail(T t){
    return !isPass(t);
  }

  default List<T> passList(Collection<T> collection){
    return passStream(collection).collect(toImmutableList());
  }

  default Set<T> passSet(Collection<T> collection){
    return passStream(collection).collect(toImmutableSet());
  }

  default List<T> failList(Collection<T> collection){
    return failStream(collection).collect(toImmutableList());
  }

  default Set<T> failSet(Collection<T> collection){
    return failStream(collection).collect(toImmutableSet());
  }

  default Stream<T> passStream(Collection<T> collection){
    return collection.stream()
        .filter(this::isPass);
  }

  default Stream<T> failStream(Collection<T> collection){
    return collection.stream()
        .filter(this::isFail);
  }

  static <T> Filter<T> passThrough(){
    return new Filter<T>() {

      @Override public boolean isPass(T t) {
        return true;
      }
    };

  }

  static <T> Filter<T> cascade(Filter<T> ...filters){
    return new Filter<T>() {

      @Override
      public boolean isPass(T t) {
        for (val filter : filters){
          val result = filter.isPass(t);
          if (!result){
            return false;
          }
        }
        return true;
      }
    };
  }


}
