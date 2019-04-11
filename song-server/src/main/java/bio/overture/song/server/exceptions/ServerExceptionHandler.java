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

package bio.overture.song.server.exceptions;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.exceptions.SongError;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.catalina.connector.Response;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;

import static bio.overture.song.core.exceptions.ServerErrors.*;
import static com.google.common.base.Throwables.getRootCause;
import static com.google.common.base.Throwables.getStackTraceAsString;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.icgc.dcc.common.core.util.Splitters.NEWLINE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@RestController
public class ServerExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String AMPERSAND = "&";
  private static final String QUESTION_MARK = "?";

  @ExceptionHandler(ServerException.class)
  public ResponseEntity<String> handleServerException(HttpServletRequest request, HttpServletResponse response,
    ServerException ex) {
    val baseSongError = ex.getSongError();
    val modifiedSongError = SongError.builder()
      .debugMessage(baseSongError.getDebugMessage())
      .httpStatusCode(baseSongError.getHttpStatusCode())
      .httpStatusName(baseSongError.getHttpStatusName())
      .message(baseSongError.getMessage())
      .stackTrace(baseSongError.getStackTrace())
      .timestamp(baseSongError.getTimestamp())
      .errorId(baseSongError.getErrorId())
      .requestUrl(generateRequestUrlWithParams(request))
      .build();
    log.error(modifiedSongError.toPrettyJson());
    return modifiedSongError.getResponseEntity();
  }

  public static ResponseEntity<String> getEgoIsDownResponse(HttpServletRequest request, HttpServletResponse response,
    Throwable ex) {
    val rootCause = getRootCause(ex);
    val error = SongError.builder()
      .requestUrl(generateRequestUrlWithParams(request))
      .timestamp(System.currentTimeMillis())
      .httpStatusCode(EGO_IS_DOWN.getHttpStatus().value())
      .httpStatusName(EGO_IS_DOWN.getHttpStatus().name())
      .errorId(EGO_IS_DOWN.getErrorId())
      .message("EGO is down")
      .debugMessage(format("[ROOT_CAUSE] -> %s: %s", rootCause.getClass().getName(), rootCause.getMessage()))
      .stackTrace(getFullStackTraceList(ex))
      .build();
    log.error(error.toPrettyJson());
    return error.getResponseEntity();
  }

  public static ResponseEntity<String> getAccessDeniedResponse(HttpServletRequest request, HttpServletResponse response,
    Throwable ex) {
    val rootCause = getRootCause(ex);
    val error = SongError.builder()
      .requestUrl(generateRequestUrlWithParams(request))
      .timestamp(System.currentTimeMillis())
      .httpStatusCode(UNAUTHORIZED_TOKEN.getHttpStatus().value())
      .httpStatusName(UNAUTHORIZED_TOKEN.getHttpStatus().name())
      .errorId(UNAUTHORIZED_TOKEN.getErrorId())
      .message(ex.getMessage())
      .debugMessage(format("[ROOT_CAUSE] -> %s: %s", rootCause.getClass().getName(), rootCause.getMessage()))
      .stackTrace(getFullStackTraceList(ex))
      .build();
    log.error(error.toPrettyJson());
    return error.getResponseEntity();
  }

  public static ResponseEntity<String> getErrorResponse(HttpServletRequest request, HttpServletResponse response,
    Throwable ex) {
    val rootCause = getRootCause(ex);
    val error = SongError.builder()
      .requestUrl(generateRequestUrlWithParams(request))
      .timestamp(System.currentTimeMillis())
      .httpStatusCode(UNKNOWN_ERROR.getHttpStatus().value())
      .httpStatusName(UNKNOWN_ERROR.getHttpStatus().name())
      .errorId(UNKNOWN_ERROR.getErrorId())
      .message(ex.getMessage())
      .debugMessage(format("[ROOT_CAUSE] -> %s: %s", rootCause.getClass().getName(), rootCause.getMessage()))
      .stackTrace(getFullStackTraceList(ex))
      .build();
    log.error(error.toPrettyJson());
    return error.getResponseEntity();
  }

  public static void report(Response response, String text) {
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
      out.write(text);
      out.close();
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  private static List<String> getFullStackTraceList(Throwable t) {
    return NEWLINE.splitToList(getStackTraceAsString(t))
      .stream()
      .map(String::trim)
      .collect(toImmutableList());
  }

  private static String generateRequestUrlWithParams(HttpServletRequest request) {
    val requestUrl = request.getRequestURL().toString();
    val paramEntries = request.getParameterMap().entrySet();
    if (paramEntries.size() > 0) {
      val params = paramEntries.stream()
        .map(x -> createUrlParams(x.getKey(), x.getValue()))
        .flatMap(Collection::stream)
        .collect(joining(AMPERSAND));
      return requestUrl + QUESTION_MARK + params;
    }
    return requestUrl;
  }

  private static List<String> createUrlParams(String key, String... values) {
    return stream(values)
      .map(x -> createUrlParam(key, x))
      .collect(toImmutableList());
  }

  private static String createUrlParam(String key, String value) {
    return format("%s=%s", key, value);
  }

}
