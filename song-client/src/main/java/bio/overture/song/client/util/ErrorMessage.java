package bio.overture.song.client.util;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.song.core.utils.JsonUtils;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value
@Builder
public class ErrorMessage {
  @NonNull private final String name;
  @NonNull private final String message;
  @NonNull private final List<String> stackTrace;
  private final long timestamp;
  @NonNull private final Date date;

  public static ErrorMessage fromException(Throwable t) {
    val d = Date.from(Instant.now());
    return ErrorMessage.builder()
        .date(d)
        .timestamp(d.getTime())
        .name(t.getClass().getSimpleName())
        .message(t.getMessage())
        .stackTrace(extractStackTrace(t))
        .build();
  }

  public String toPrettyJson() {
    return JsonUtils.toPrettyJson(this);
  }

  private static List<String> extractStackTrace(Throwable t) {
    return stream(t.getStackTrace()).map(StackTraceElement::toString).collect(toUnmodifiableList());
  }
}
