package org.icgc.dcc.song.core.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.groupingBy;

public class Reductions {

  public static <K,T> Map<K, T> groupUnique(List<T> list, Function<T, K> keyMapper){
    val map = list.stream().collect(groupingBy(keyMapper));
    val outMap = ImmutableMap.<K,T>builder();
    for (val entry : map.entrySet()){
      checkState(entry.getValue().size() == 1,
          "Collision detect for key '%s' for the values:\n\t%s",
          entry.getKey(), Joiner.on("\n\t").join(entry.getValue()) );
      outMap.put(entry.getKey(), entry.getValue().get(0));
    }
    return outMap.build();
  }
}
