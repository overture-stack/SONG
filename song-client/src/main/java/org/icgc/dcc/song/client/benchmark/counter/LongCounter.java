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

package org.icgc.dcc.song.client.benchmark.counter;

import lombok.val;

public class LongCounter implements Counter<Long>{

  private final long init;

  private long count;

  public LongCounter(long init) {
    this.init = init;
    this.count = init;
  }

  @Override
  public Long preIncr() {
    return ++count;
  }

  @Override
  public Long preIncr(Long amount) {
    count += amount;
    return count;
  }

  @Override
  public void reset() {
    count = init;
  }

  @Override
  public Long getCount() {
    return count;
  }

  @Override
  public Long postIncr() {
    return count++;
  }

  @Override
  public Long postIncr(Long amount) {
    val post = count;
    count += amount;
    return post;
  }

  public static LongCounter createLongCounter0() {
    return createLongCounter(0L);
  }

  public static LongCounter createLongCounter(long init) {
    return new LongCounter(init);
  }

}
