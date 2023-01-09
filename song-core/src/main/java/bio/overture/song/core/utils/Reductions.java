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

package bio.overture.song.core.utils;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.groupingBy;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.val;

public class Reductions {

  public static <K, T> Map<K, T> groupUnique(List<T> list, Function<T, K> keyMapper) {
    val map = list.stream().collect(groupingBy(keyMapper));
    val outMap = ImmutableMap.<K, T>builder();
    for (val entry : map.entrySet()) {
      checkState(
          entry.getValue().size() == 1,
          "Collision detect for key '%s' for the values:\n\t%s",
          entry.getKey(),
          Joiner.on("\n\t").join(entry.getValue()));
      outMap.put(entry.getKey(), entry.getValue().get(0));
    }
    return outMap.build();
  }
}
