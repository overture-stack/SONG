package bio.overture.song.server.config;

import bio.overture.song.server.exceptions.CustomErrorPageFilter;
import bio.overture.song.server.exceptions.CustomErrorReportValve;
import lombok.val;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Configuration
public class ErrorConfig {
//  @Bean
//  public FilterRegistrationBean<CustomErrorPageFilter> myFilter() {
//    FilterRegistrationBean<CustomErrorPageFilter> registration = new FilterRegistrationBean<>();
//    registration.setFilter(new CustomErrorPageFilter());
//    registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
//    return registration;
//  }
//
//  @Bean
//  public ErrorProperties myErrorProperties() {
//    val p = new ErrorProperties();
//    p.setIncludeStacktrace(ErrorProperties.IncludeStacktrace.ALWAYS);
//    p.setIncludeException(true);
//    val w = new ErrorProperties.Whitelabel();
//    w.setEnabled(false);
//    p.setWhitelabel(w);
//    return p;
//  }

  @Bean
  public ErrorReportValve myCustomValve() {
    return new CustomErrorReportValve();
  }
}
