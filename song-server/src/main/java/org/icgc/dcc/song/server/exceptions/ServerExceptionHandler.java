package org.icgc.dcc.song.server.exceptions;

import lombok.val;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.lang.String.format;

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


}
