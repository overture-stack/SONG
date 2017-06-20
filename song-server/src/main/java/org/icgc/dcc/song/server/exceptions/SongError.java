package org.icgc.dcc.song.server.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.Setter;
import lombok.val;
import org.icgc.dcc.song.server.utils.JsonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static lombok.AccessLevel.NONE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.icgc.dcc.song.server.utils.Debug.streamCallingStackTrace;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "errorId", "httpStatusCode", "httpStatusName",
    "requestUrl", "datetime", "timestamp",  "message", "debugMessage", "stackTrace" })
@Data
public class SongError {

  private static final String NOT_AVAILABLE = "N/A";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = ISO_ZONED_DATE_TIME;
  private static final ZoneId ZONE_ID = ZoneId.systemDefault();

  private List<String> stackTrace;
  private String errorId;
  private String httpStatusName;
  private int httpStatusCode;
  private String message;
  private String requestUrl;
  private String debugMessage;
  private long timestamp;
  @Setter(NONE) private String datetime;

  public void setHttpStatus(HttpStatus httpStatus){
    this.httpStatusCode = httpStatus.value();
    this.httpStatusName = httpStatus.name();
  }

  public void setTimestamp(long timestamp){
    this.timestamp = timestamp;
    val instant = Instant.ofEpochMilli(timestamp);
    this.datetime = ZonedDateTime.ofInstant(instant, ZONE_ID).format(DATE_TIME_FORMATTER);
  }

  @JsonIgnore
  public String toJson(){
    return JsonUtils.toJson(this);
  }

  public void setStackTraceElementArray(StackTraceElement[] stackTrace){
    setStackTraceElementList(stream(stackTrace).collect(toImmutableList()));
  }

  public void setStackTraceElementList(List<StackTraceElement> stackTrace){
    this.stackTrace = stackTrace.stream().map(Object::toString).collect(toImmutableList());
  }

  @JsonIgnore
  public ResponseEntity<String> getResponseEntity(){
    return ResponseEntity.status(httpStatusCode).body(toJson());
  }

  public static ResponseEntity<String> error(ServerError serverError, String format, Object... args){
    val st = streamCallingStackTrace()
        .skip(1)
        .collect(toImmutableList());
    val error = new SongError();
    error.setMessage(format(format,args));
    error.setErrorId(serverError.getErrorId());
    error.setHttpStatus(serverError.getHttpStatus());
    error.setStackTraceElementList(st);
    error.setTimestamp(currentTimeMillis());
    error.setRequestUrl(NOT_AVAILABLE);
    error.setDebugMessage(NOT_AVAILABLE);
    return error.getResponseEntity();
  }

}
