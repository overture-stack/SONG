package org.icgc.dcc.sodalite.client;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.val;

@SpringBootApplication
public class SpringApp {

  public static void main(String... args) {
    val app = new SpringApplication(SpringApp.class);
    app.setBannerMode(Banner.Mode.OFF);
    app.setWebEnvironment(false);
    app.setLogStartupInfo(false);
    app.setRegisterShutdownHook(false);
    app.setAddCommandLineProperties(false);
    app.run(args);
  }

}
