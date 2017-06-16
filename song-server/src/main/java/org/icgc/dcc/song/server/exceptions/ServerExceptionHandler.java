package org.icgc.dcc.song.server.exceptions;

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
    val requestUrl = request.getRequestURL().toString();
    val error = ex.getError();
    error.setRequestUrl(requestUrl);
    response.setStatus(error.getHttpStatus().value());
    return error.toObjectNode().toString();
  }

  private static String getServerUrl(HttpServletRequest request){
    return "sdf";

  }

}
