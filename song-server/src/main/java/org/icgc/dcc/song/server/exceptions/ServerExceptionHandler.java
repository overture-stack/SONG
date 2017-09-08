package org.icgc.dcc.song.server.exceptions;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.icgc.dcc.song.core.exceptions.SongError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Splitters.NEWLINE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;

@Slf4j
@ControllerAdvice
public class ServerExceptionHandler {

  @ExceptionHandler(ServerException.class)
  public ResponseEntity<String> handleServerException(HttpServletRequest request, HttpServletResponse response, ServerException ex){
    val requestUrl = request.getRequestURL().toString();
    val songError = ex.getSongError();
    songError.setRequestUrl(requestUrl);
    return songError.getResponseEntity();
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<String> handleThrowable(HttpServletRequest request, HttpServletResponse response, Throwable ex){
    val requestUrl = request.getRequestURL().toString();
    val error = new SongError();
    error.setRequestUrl(requestUrl);
    error.setTimestamp(System.currentTimeMillis());
    error.setHttpStatus(UNKNOWN_ERROR.getHttpStatus());
    error.setErrorId(UNKNOWN_ERROR.getErrorId());
    error.setMessage(ex.getMessage());
    val rootCause = Throwables.getRootCause(ex);
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


}
