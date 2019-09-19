package bio.overture.song.core.utils;

import static java.util.Objects.isNull;
import static java.util.stream.IntStream.range;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;

public class CollectionUtils {

  public static <T, U> Set<U> mapToImmutableSet(Collection<T> collection, Function<T, U> mapper) {
    return collection.stream().map(mapper).collect(toImmutableSet());
  }

  public static <T, U> List<U> mapToImmutableList(Collection<T> collection, Function<T, U> mapper) {
    return collection.stream().map(mapper).collect(toImmutableList());
  }

  public static <T> List<T> repeatedCallsOf(@NonNull Supplier<T> callback, int numberOfCalls) {
    return range(0, numberOfCalls).boxed().map(x -> callback.get()).collect(toImmutableList());
  }

  public static boolean isCollectionBlank(Collection values) {
    return isNull(values) || values.isEmpty();
  }

  public static <T> boolean isArrayBlank(T[] values) {
    return isNull(values) || values.length == 0;
  }
}
