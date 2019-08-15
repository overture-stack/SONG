/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.core.exceptions;

import static bio.overture.song.core.utils.Debug.streamCallingStackTrace;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.Responses.contextMessage;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

import bio.overture.song.core.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
  "errorId",
  "httpStatusCode",
  "httpStatusName",
  "requestUrl",
  "datetime",
  "timestamp",
  "message",
  "debugMessage",
  "stackTrace"
})
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
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

  public void setHttpStatus(HttpStatus httpStatus) {
    this.httpStatusCode = httpStatus.value();
    this.httpStatusName = httpStatus.name();
  }

  public String getDatetime() {
    val instant = Instant.ofEpochMilli(timestamp);
    return ZonedDateTime.ofInstant(instant, ZONE_ID).format(DATE_TIME_FORMATTER);
  }

  @Override
  public String toString() {
    return format("SONG_SERVER_ERROR[%s @ %s]: %s", getErrorId(), getTimestamp(), getMessage());
  }

  @JsonIgnore
  public String toJson() {
    return JsonUtils.toJson(this);
  }

  @JsonIgnore
  public String toPrettyJson() {
    return JsonUtils.toPrettyJson(this);
  }

  public void setStackTraceElementArray(StackTraceElement[] stackTrace) {
    setStackTraceElementList(stream(stackTrace).collect(toImmutableList()));
  }

  public void setStackTraceElementList(List<StackTraceElement> stackTrace) {
    this.stackTrace = stackTrace.stream().map(Object::toString).collect(toImmutableList());
  }

  @JsonIgnore
  public ResponseEntity<String> getResponseEntity() {
    return ResponseEntity.status(httpStatusCode).body(toJson());
  }

  public static ResponseEntity<String> error(
      Class<?> clazz, ServerError serverError, String format, Object... args) {
    return error(clazz.getSimpleName(), serverError, format, args);
  }

  public static ResponseEntity<String> error(
      String context, ServerError serverError, String format, Object... args) {
    return error(
        serverError, contextMessage(context + "::" + serverError.getErrorId(), format, args));
  }

  public static SongError createSongError(
      Class<?> clazz, ServerError serverError, String formattedMessage, Object... args) {
    return createSongError(clazz.getSimpleName(), serverError, formattedMessage, args);
  }

  public static SongError createSongError(
      String context, ServerError serverError, String formattedMessage, Object... args) {
    return createSongError(
        serverError,
        contextMessage(context + "::" + serverError.getErrorId(), formattedMessage, args));
  }

  public static SongError createSongError(
      @NonNull ServerError serverError, @NonNull String formattedMessage, Object... args) {
    return createSongError(serverError, NOT_AVAILABLE, NOT_AVAILABLE, formattedMessage, args);
  }

  public static SongError createSongError(
      @NonNull ServerError serverError,
      @NonNull String requestUrl,
      @NonNull String debugMessage,
      @NonNull String formattedMessage,
      Object... args) {
    val st = streamCallingStackTrace().skip(1).map(Object::toString).collect(toImmutableList());
    return SongError.builder()
        .message(format(formattedMessage, args))
        .errorId(serverError.getErrorId())
        .httpStatusCode(serverError.getHttpStatus().value())
        .httpStatusName(serverError.getHttpStatus().name())
        .stackTrace(st)
        .timestamp(currentTimeMillis())
        .requestUrl(requestUrl)
        .debugMessage(debugMessage)
        .build();
  }

  public static ResponseEntity<String> error(
      ServerError serverError, String formattedMessage, Object... args) {
    return createSongError(serverError, formattedMessage, args).getResponseEntity();
  }

  public static SongError parseErrorResponse(int httpStatusCode, String body) {
    val httpStatus = HttpStatus.valueOf(httpStatusCode);
    return parseErrorResponse(httpStatus, body);
  }

  public static SongError parseErrorResponse(HttpStatus httpStatus, String body) {
    checkState(
        httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
        "Cannot create an error for [%s] since it is not a 400 series client error or a 500 series server error ",
        httpStatus.toString());
    return fromJson(body, SongError.class);
  }

  public static SongError parseErrorResponse(ResponseEntity<String> responseEntity) {
    val httpStatus = responseEntity.getStatusCode();
    val body = responseEntity.getBody();
    return parseErrorResponse(httpStatus, body);
  }

  public static SongError parseErrorResponse(ClientHttpResponse response) throws IOException {
    val httpStatus = response.getStatusCode();
    val br = new BufferedReader(new InputStreamReader(response.getBody()));
    val body = br.lines().collect(Collectors.joining(""));
    return parseErrorResponse(httpStatus, body);
  }
}
