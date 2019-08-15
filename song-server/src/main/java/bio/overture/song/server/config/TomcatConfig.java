package bio.overture.song.server.config;

import bio.overture.song.server.exceptions.CustomErrorReportValve;
import java.util.Collections;
import lombok.val;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {
  @Bean
  public ServletWebServerFactory servletContainer() {
    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
    val err = new CustomErrorReportValve();
    tomcat.setContextValves(Collections.singletonList(err));
    return tomcat;
  }
}
