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

import bio.overture.song.core.exceptions.ServerErrors;
import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.exceptions.SongError;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.everit.json.schema.ValidationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Throwables.getRootCause;
import static com.google.common.base.Throwables.getStackTraceAsString;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.icgc.dcc.common.core.util.Splitters.NEWLINE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static bio.overture.song.core.exceptions.ServerErrors.BAD_REPLY_FROM_GATEWAY;
import static bio.overture.song.core.exceptions.ServerErrors.GATEWAY_IS_DOWN;
import static bio.overture.song.core.exceptions.ServerErrors.GATEWAY_SERVICE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.GATEWAY_TIMED_OUT;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.exceptions.ServerErrors.UNAUTHORIZED_TOKEN;
import static bio.overture.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@RestController
public class ServerExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String AMPERSAND = "&";
  private static final String QUESTION_MARK = "?";

  static void report(Request request, Response response, Throwable t) {

    writeResponse(response, getJsonErrorMessage(request, t));
  }

  static String getJsonErrorMessage(Request request, Throwable t) {
    if (t.getCause() instanceof ConnectException) {
      return errorResponseBody(request, t, GATEWAY_IS_DOWN);
    } else if (t instanceof HttpStatusCodeException) {
      return getHttpErrorMessage(request, (HttpStatusCodeException) t);
    } else if (t.getCause() instanceof HttpMessageNotReadableException) {
      return errorResponseBody(request, t, BAD_REPLY_FROM_GATEWAY);
    } else if (t.getCause() instanceof ServerException) {
      return getServerExceptionResponse(request, (ServerException) t).getBody();
    } else {
      return errorResponseBody(request, t, UNKNOWN_ERROR);
    }
  }

  static String getHttpErrorMessage(Request request,
    HttpStatusCodeException ex) {
    val code = ex.getStatusCode();
    if (code == HttpStatus.UNAUTHORIZED || code == HttpStatus.FORBIDDEN) {
      return errorResponseBody(request, ex, UNAUTHORIZED_TOKEN);
    } else if (code == HttpStatus.NOT_FOUND) {
      return errorResponseBody(request, ex, GATEWAY_SERVICE_NOT_FOUND);
    } else if (code == HttpStatus.GATEWAY_TIMEOUT) {
      return errorResponseBody(request, ex, GATEWAY_TIMED_OUT);
    } else {
      return getHttpErrorResponse(request, ex);
    }
  }

  private static void writeResponse(Response response, String content) {
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
      out.write(content);
      out.close();
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<String> handleValidationException(HttpServletRequest request, HttpServletResponse response,
      ValidationException ex){
    return songErrorResponse(request, ex, SCHEMA_VIOLATION).getResponseEntity();
  }

  @ExceptionHandler(ServerException.class)
  public ResponseEntity<String> handleServerException(HttpServletRequest request, HttpServletResponse response,
    ServerException ex) {
    return getServerExceptionResponse(request, ex);
  }

  private static SongError.SongErrorBuilder withRequestUrl(HttpServletRequest request, SongError e) {
    return e.toBuilder().requestUrl(generateRequestUrlWithParams(request));
  }

  private static ResponseEntity<String> getServerExceptionResponse(HttpServletRequest request, ServerException ex) {
    val modifiedSongError = withRequestUrl(request, ex.getSongError()).build();
    log.error(modifiedSongError.toPrettyJson());
    return modifiedSongError.getResponseEntity();
  }

  private static String getHttpErrorResponse(HttpServletRequest request, HttpStatusCodeException ex) {
    val code = ex.getStatusCode();
    return errorResponseBody(request, ex, code, code.name(), ex.getMessage());
  }

  private static SongError songErrorResponse(HttpServletRequest request, Throwable ex, ServerErrors errors) {
    return songErrorResponse(request, ex, errors.getHttpStatus(), errors.getErrorId(), ex.getMessage());
  }

  private static String errorResponseBody(HttpServletRequest request, Throwable ex, ServerErrors errors) {
    return errorResponseBody(request, ex, errors.getHttpStatus(), errors.getErrorId(), ex.getMessage());
  }

  private static SongError songErrorResponse(HttpServletRequest request,
      Throwable ex, HttpStatus code, String err, String msg) {
    val rootCause = getRootCause(ex);
    val error = SongError.builder()
        .requestUrl(generateRequestUrlWithParams(request))
        .timestamp(System.currentTimeMillis())
        .httpStatusCode(code.value())
        .httpStatusName(code.name())
        .errorId(err)
        .message(msg)
        .debugMessage(format("[ROOT_CAUSE] -> %s: %s", rootCause.getClass().getName(), rootCause.getMessage()))
        .stackTrace(getFullStackTraceList(ex))
        .build();
    log.error(error.toPrettyJson());
    return error;
  }

  private static String errorResponseBody(HttpServletRequest request,
    Throwable ex, HttpStatus code, String err, String msg) {
    val error =  songErrorResponse(request, ex, code, err, msg);
    return error.getResponseEntity().getBody();
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
