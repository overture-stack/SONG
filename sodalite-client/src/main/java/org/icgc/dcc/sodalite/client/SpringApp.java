package org.icgc.dcc.sodalite.client;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringApp {
	
	public static void main(String... args) {
		SpringApplication app = new SpringApplication(SpringApp.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.setWebEnvironment(false);
		app.setLogStartupInfo(false);
		app.setRegisterShutdownHook(false);
		app.setAddCommandLineProperties(false);
		app.run(args);
	}
	
}
