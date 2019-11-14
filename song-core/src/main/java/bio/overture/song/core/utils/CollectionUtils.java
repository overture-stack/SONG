package bio.overture.song.core.utils;

import lombok.NonNull;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.IntStream.range;

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

  // a - b
  public static <T> List<T> listDifference(@NonNull List<T> a, @NonNull List<T> b){
    val out = new ArrayList<>(a);
    out.removeAll(b);
    return out;
  }
}
