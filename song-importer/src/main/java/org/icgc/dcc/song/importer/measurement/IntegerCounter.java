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

package org.icgc.dcc.song.importer.measurement;

import java.util.Collection;

public class IntegerCounter implements Countable<Integer> {

  private static final int DEFAULT_INIT_VAL = 0;

  private final int initVal;
  private int count;

  public IntegerCounter(final int initVal) {
    this.initVal = initVal;
    this.count = initVal;
  }

  @Override
  public void incr() {
    count++;
  }

  @Override
  public void incr(final Integer amount) {
    count += amount;
  }

  @Override
  public void reset() {
    count = initVal;
  }

  @Override
  public Integer getCount() {
    return count;
  }

  @Override
  public <C extends Collection<?>> C streamCollectionCount(C objects) {
    this.incr(objects.size());
    return objects;
  }

  public static IntegerCounter newIntegerCounter(final int initVal){
    return new IntegerCounter(initVal);
  }

  public static IntegerCounter newDefaultIntegerCounter(){
    return newIntegerCounter(DEFAULT_INIT_VAL);
  }


}
