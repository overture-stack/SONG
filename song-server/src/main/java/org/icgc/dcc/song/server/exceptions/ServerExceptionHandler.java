package org.icgc.dcc.song.server.exceptions;

import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.icgc.dcc.song.server.exceptions.ServerErrors.UNKNOWN_ERROR;

@ControllerAdvice
public class ServerExceptionHandler {

  @ExceptionHandler(ServerException.class)
  @ResponseBody
  public String handleServerException(HttpServletRequest request, HttpServletResponse response, ServerException ex){
    val requestUrl = request.getRequestURL().toString();
    val error = ex.getError();
    error.setRequestUrl(requestUrl);
    response.setStatus(error.getHttpStatusCode());
    return error.toJson();
  }

  @ExceptionHandler(Throwable.class)
  @ResponseBody
  public ResponseEntity<String> handleThrowable(HttpServletRequest request, HttpServletResponse response, Throwable ex){
    val requestUrl = request.getRequestURL().toString();
    val error = new SongError();
    error.setRequestUrl(requestUrl);
    error.setTimestamp(System.currentTimeMillis());
    error.setHttpStatus(UNKNOWN_ERROR.getHttpStatus());
    error.setErrorId(UNKNOWN_ERROR.getErrorId());
    error.setMessage(ex.getMessage());
    error.setStackTraceElementArray(ex.getStackTrace());
    return error.getResponseEntity();
  }


}
