package org.icgc.dcc.song.server.exceptions;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.icgc.dcc.song.core.exceptions.SongError;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Throwables.getRootCause;
import static com.google.common.base.Throwables.getStackTraceAsString;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.icgc.dcc.common.core.util.Splitters.NEWLINE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNAUTHORIZED_TOKEN;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;

@Slf4j
@ControllerAdvice
public class ServerExceptionHandler {

  private static final String AMPERSAND = "&";
  private static final String QUESTION_MARK= "?";

  @ExceptionHandler(ServerException.class)
  public ResponseEntity<String> handleServerException(HttpServletRequest request, HttpServletResponse response, ServerException ex){
    val songError = ex.getSongError();
    songError.setRequestUrl(generateRequestUrlWithParams(request));
    log.error(songError.toPrettyJson());
    return songError.getResponseEntity();
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<String> handleAccessDeniedException(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException ex){
    val error = new SongError();
    error.setRequestUrl(generateRequestUrlWithParams(request));
    error.setTimestamp(System.currentTimeMillis());
    error.setHttpStatus(UNAUTHORIZED_TOKEN.getHttpStatus());
    error.setErrorId(UNAUTHORIZED_TOKEN.getErrorId());
    error.setMessage(ex.getMessage());
    val rootCause = getRootCause(ex);
    error.setDebugMessage(format("[ROOT_CAUSE] -> %s: %s", rootCause.getClass().getName(), rootCause.getMessage()));
    error.setStackTrace(getFullStackTraceList(ex));
    log.error(error.toPrettyJson());
    return error.getResponseEntity();
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<String> handleThrowable(HttpServletRequest request, HttpServletResponse response, Throwable ex){
    val error = new SongError();
    error.setRequestUrl(generateRequestUrlWithParams(request));
    error.setTimestamp(System.currentTimeMillis());
    error.setHttpStatus(UNKNOWN_ERROR.getHttpStatus());
    error.setErrorId(UNKNOWN_ERROR.getErrorId());
    error.setMessage(ex.getMessage());
    val rootCause = getRootCause(ex);
    error.setDebugMessage(format("[ROOT_CAUSE] -> %s: %s", rootCause.getClass().getName(), rootCause.getMessage()));
    error.setStackTrace(getFullStackTraceList(ex));
    log.error(error.toPrettyJson());
    return error.getResponseEntity();
  }

  private static List<String> getFullStackTraceList(Throwable t){
    return NEWLINE.splitToList(getStackTraceAsString(t))
        .stream()
        .map(String::trim)
        .collect(toImmutableList());
  }

  private static String generateRequestUrlWithParams(HttpServletRequest request){
    val requestUrl = request.getRequestURL().toString();
    val paramEntries = request.getParameterMap().entrySet();
    if (paramEntries.size() > 0){
      val params = paramEntries.stream()
          .map(x -> createUrlParams(x.getKey(), x.getValue()))
          .flatMap(Collection::stream)
          .collect(joining(AMPERSAND));
      return requestUrl+QUESTION_MARK+params;
    }
    return requestUrl;
  }

  private static List<String> createUrlParams(String key, String ... values){
    return stream(values)
        .map(x -> createUrlParam(key, x))
        .collect(toImmutableList());
  }

  private static String createUrlParam(String key, String value){
    return format("%s=%s", key, value);
  }


}
