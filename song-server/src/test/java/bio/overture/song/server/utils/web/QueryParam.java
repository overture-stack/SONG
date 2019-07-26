package bio.overture.song.server.utils.web;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collection;

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;

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
