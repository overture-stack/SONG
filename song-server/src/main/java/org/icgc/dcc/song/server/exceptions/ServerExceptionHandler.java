package org.icgc.dcc.song.server.exceptions;

import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.lang.String.format;
import static org.icgc.dcc.song.server.exceptions.ServerErrors.UNKNOWN_ERROR;

@ControllerAdvice
public class ServerExceptionHandler {

  @ExceptionHandler(ServiceException.class)
  @ResponseBody
  public String handleServiceException(HttpServletRequest request, HttpServletResponse response, ServiceException ex){
    val requestUrl = request.getRequestURL().toString();
    val error = ex.getError();
    error.setRequestUrl(requestUrl);
    response.setStatus(error.getHttpStatus().value());
    val modifiedMessage = format("[%s]: %s",ex.getService().getName(), error.getMessage());
    error.setMessage(modifiedMessage);
    return error.toObjectNode().toString();
  }

  @ExceptionHandler(ServerException.class)
  @ResponseBody
  public String handleServerException(HttpServletRequest request, HttpServletResponse response, ServerException ex){
    val requestUrl = request.getRequestURL().toString();
    val error = ex.getError();
    error.setRequestUrl(requestUrl);
    response.setStatus(error.getHttpStatus().value());
    return error.toObjectNode().toString();
  }

  @ExceptionHandler(Throwable.class)
  @ResponseBody
  public ResponseEntity<String> handleThrowable(HttpServletRequest request, HttpServletResponse response, Throwable ex){
    val requestUrl = request.getRequestURL().toString();
    val error = new Error();
    error.setRequestUrl(requestUrl);
    error.setTimestamp(System.currentTimeMillis());
    error.setHttpStatus(UNKNOWN_ERROR.getHttpStatus());
    error.setErrorId(UNKNOWN_ERROR.getErrorId());
    error.setMessage(ex.getMessage());
    error.setStackTrace(ex.getStackTrace());
    return ResponseEntity.status(response.getStatus()).body(error.toObjectNode().toString());
  }


}
