package bio.overture.song.server.config;

import bio.overture.song.server.exceptions.CustomErrorReportValve;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorConfig {
  @Bean
  public ErrorReportValve myCustomValve() {
    return new CustomErrorReportValve();
  }
}
