package bio.overture.song.server.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Slf4j
public class CustomErrorPageFilter extends ErrorPageFilter  {

  @Override
  public void doFilter(ServletRequest request,  ServletResponse response,
    FilterChain chain) throws IOException, ServletException {
    log.error("Got request" + request.toString());
    log.error("Response=" + response.toString());
    log.error("Filtering errors so we have a breakpoint to hit!");
    super.doFilter(request,response, chain);
  }
}
