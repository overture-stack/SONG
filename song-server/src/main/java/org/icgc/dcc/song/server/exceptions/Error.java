package org.icgc.dcc.song.server.exceptions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.array;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.icgc.dcc.song.server.utils.Debug.streamCallingStackTrace;

@Data
public class Error {

  private static final String NOT_AVAILABLE = "N/A";

  //TODO: annotate this with proper Jackson Json annotations, instead of manually creating field names
  private List<StackTraceElement> stackTrace;
  private String errorId;
  private HttpStatus httpStatus;
  private String message;
  private String requestUrl;
  private String debugMessage;
  private long timestamp;

  public ObjectNode toObjectNode(){
    val date = new Date(timestamp);
    return object()
        .with("httpStatus", getHttpStatus().value())
        .with("errorId", getErrorId())
        .with("date", date.toString())
        .with("timestamp", timestamp)
        .with("message", getMessage())
        .with("debugMessage", getDebugMessage())
        .with("requestUrl", getRequestUrl())
        .with("stackTrace", array(
            getStackTrace()
                .stream()
                .map(Object::toString)
                .collect(toImmutableList())))
        .end();
  }

  //TODO: create parse static method, to take a json object and create an Error instance

  public void setStackTrace(StackTraceElement[] stackTrace){
    setStackTrace(stream(stackTrace).collect(toImmutableList()));
  }

  public void setStackTrace(List<StackTraceElement> stackTrace){
    this.stackTrace = stackTrace;
  }

  public ResponseEntity<String> getResponseEntity(){
    return ResponseEntity.status(getHttpStatus()).body(toObjectNode().toString());
  }

  public static ResponseEntity<String> error(ServerError serverError, String format, Object... args){
    val st = streamCallingStackTrace()
        .skip(1)
        .collect(toImmutableList());
    val error = new Error();
    error.setMessage(format(format,args));
    error.setErrorId(serverError.getErrorId());
    error.setHttpStatus(serverError.getHttpStatus());
    error.setStackTrace(st);
    error.setTimestamp(currentTimeMillis());
    error.setRequestUrl(NOT_AVAILABLE);
    error.setDebugMessage(NOT_AVAILABLE);
    return error.getResponseEntity();
  }

}
