package org.icgc.dcc.song.server.repository.exceptions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.springframework.http.HttpStatus;

import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.common.core.util.Joiners.NEWLINE;

@Data
public class Error {
  private StackTraceElement[] stackTrace;
  private String id;
  private HttpStatus httpStatus;
  private String message;
  private String url;
  private String debugMessage;

  public ObjectNode toObjectNode(){
    return object()
        .with("httpStatus", getHttpStatus().value())
        .with("id", getId())
        .with("message", getMessage())
        .with("debugMessage", getDebugMessage())
        .with("url", getUrl())
        .with("stackTrace", NEWLINE.join(getStackTrace()))
        .end();
  }

}
