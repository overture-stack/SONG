package bio.overture.song.server.exceptions;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;

public class CustomErrorReportValve extends ErrorReportValve {
  @Override
  protected void report(Request request, Response response, Throwable t) {
    ServerExceptionHandler.report(request, response, t);
  }
}