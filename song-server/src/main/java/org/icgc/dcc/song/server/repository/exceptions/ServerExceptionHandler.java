package org.icgc.dcc.song.server.repository.exceptions;

import lombok.val;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ServerExceptionHandler {

  @ExceptionHandler(ServerException.class)
  @ResponseBody
  public String handleServerException(HttpServletRequest request, HttpServletResponse response, ServerException ex){
    val url = request.getRequestURI();
    val error = ex.getError();
    error.setUrl(url);
    response.setStatus(error.getHttpStatus().value());
    return error.toObjectNode().toString();
  }

}
