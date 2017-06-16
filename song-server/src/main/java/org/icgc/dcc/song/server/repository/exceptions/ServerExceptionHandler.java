package org.icgc.dcc.song.server.repository.exceptions;

import lombok.val;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ServerExceptionHandler {

  @ExceptionHandler(ServerException.class)
  @ResponseBody
  public String handleServerException(HttpServletRequest request, ServerException e){
    val url = e.getUrl();
    val error = e.getError();
    error.setUrl(url);
    return error.toObjectNode().toString();
  }

}
