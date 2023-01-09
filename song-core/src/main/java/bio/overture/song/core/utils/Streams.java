package bio.overture.song.core.utils;

import static lombok.AccessLevel.PRIVATE;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = PRIVATE)
public class Streams {

  public static <T> Stream<T> stream(@NonNull Iterator<T> iterator) {
    return stream(() -> iterator, false);
  }

  public static <T> Stream<T> stream(@NonNull Iterable<T> iterable) {
    return stream(iterable, false);
  }

  @SafeVarargs
  public static <T> Stream<T> stream(@NonNull T... values) {
    return List.of(values).stream();
  }

  @SuppressWarnings("unchecked")
  public static <T, R> Function<T, Stream<? extends R>> stream(
      @NonNull Function<? super T, ? extends Iterable<? extends R>> mapper) {
    return (x) -> stream((Iterable) mapper.apply(x));
  }

  private static <T> Stream<T> stream(Iterable<T> iterable, boolean inParallel) {
    return StreamSupport.stream(iterable.spliterator(), inParallel);
  }
}
