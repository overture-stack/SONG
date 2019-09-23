package bio.overture.song.core.utils;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.IntStream.range;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;

public class CollectionUtils {

  public static <T, U> Set<U> mapToImmutableSet(Collection<T> collection, Function<T, U> mapper) {
    return collection.stream().map(mapper).collect(toUnmodifiableSet());
  }

  public static <T, U> List<U> mapToImmutableList(Collection<T> collection, Function<T, U> mapper) {
    return collection.stream().map(mapper).collect(toUnmodifiableList());
  }

  public static <T> List<T> repeatedCallsOf(@NonNull Supplier<T> callback, int numberOfCalls) {
    return range(0, numberOfCalls).boxed().map(x -> callback.get()).collect(toUnmodifiableList());
  }

  public static boolean isCollectionBlank(Collection values) {
    return isNull(values) || values.isEmpty();
  }

  public static <T> boolean isArrayBlank(T[] values) {
    return isNull(values) || values.length == 0;
  }
}
