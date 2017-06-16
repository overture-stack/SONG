package org.icgc.dcc.song.server.exceptions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.val;
import org.springframework.http.HttpStatus;

import java.util.Date;

import static org.icgc.dcc.common.core.json.JsonNodeBuilders.array;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@Data
public class Error {
  private StackTraceElement[] stackTrace;
  private String id;
  private HttpStatus httpStatus;
  private String message;
  private String requestUrl;
  private String debugMessage;
  private long timestamp;

  public ObjectNode toObjectNode(){
    val date = new Date(timestamp);
    return object()
        .with("httpStatus", getHttpStatus().value())
        .with("id", getId())
        .with("date", date.toString())
        .with("timestamp", timestamp)
        .with("message", getMessage())
        .with("debugMessage", getDebugMessage())
        .with("requestUrl", getRequestUrl())
        .with("stackTrace", array(
            stream(getStackTrace())
                .map(Object::toString)
                .collect(toImmutableList())))
        .end();
  }

}
