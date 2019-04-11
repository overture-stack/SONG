package bio.overture.song.server.exceptions;

import bio.overture.song.core.exceptions.ServerException;
import lombok.val;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.net.ConnectException;
import java.util.logging.Logger;

public class CustomErrorReportValve extends ErrorReportValve {
  Logger log = Logger.getLogger(CustomErrorReportValve.class.getName());

  @Override
  protected void report(Request request, Response response, Throwable t) {
    String s;
    if (t.getCause() instanceof ConnectException) {
      s = msg(ServerExceptionHandler.getEgoIsDownResponse(request, response, t));
    } else if (t instanceof HttpClientErrorException) {
      val ex = (HttpClientErrorException) t;
      if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED ||
        ex.getStatusCode() == HttpStatus.FORBIDDEN) {
        s = msg(ServerExceptionHandler.getAccessDeniedResponse(request, response, t));
      } else {
        s = msg(ServerExceptionHandler.getErrorResponse(request, response, t));
      }
    } else {
      s = msg(ServerExceptionHandler.getErrorResponse(request, response, t));
    }
    ServerExceptionHandler.report(response, s);
  }
  
  private String msg(ResponseEntity<String> responseEntity) {
    return responseEntity.getBody();
  }
}