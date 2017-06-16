package org.icgc.dcc.song.server.repository.exceptions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.springframework.http.HttpStatus;

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
  private String url;
  private String debugMessage;
  private long timestamp;

  public ObjectNode toObjectNode(){
//    val date = new Date(timestamp);
//    val format = new SimpleDateFormat("yyyy-MM-ddTHH-mm-ss");
//    format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
//    val dateString = format.format(date);
    return object()
        .with("httpStatus", getHttpStatus().value())
        .with("id", getId())
//        .with("date", dateString)
//        .with("timestamp", timestamp)
        .with("message", getMessage())
        .with("debugMessage", getDebugMessage())
        .with("url", getUrl())
        .with("stackTrace", array(stream(getStackTrace()).map(Object::toString).collect(toImmutableList())))
        .end();
  }

}
