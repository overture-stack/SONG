package bio.overture.song.core.web;

import static bio.overture.song.core.utils.Joiners.COMMA;
import static java.lang.String.format;

import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class QueryParam {
  @NonNull private final String key;
  @NonNull private final Object value;

  public static QueryParam createQueryParam(String key, Collection values) {
    return new QueryParam(key, COMMA.join(values));
  }

  @Override
  public String toString() {
    return format("%s=%s", key, value);
  }
}
