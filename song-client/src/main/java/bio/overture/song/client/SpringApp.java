/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package bio.overture.song.client;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;

import java.util.function.Consumer;

import static org.springframework.boot.WebApplicationType.NONE;

@SpringBootApplication(
    exclude = {
      JacksonAutoConfiguration.class,
      HttpMessageConvertersAutoConfiguration.class,
      CacheAutoConfiguration.class,
      SpringApplicationAdminJmxAutoConfiguration.class,
      CodecsAutoConfiguration.class,
      JmxAutoConfiguration.class,
      ProjectInfoAutoConfiguration.class,
      PropertyPlaceholderAutoConfiguration.class,
      RestTemplateAutoConfiguration.class,
      TaskExecutionAutoConfiguration.class,
      TaskSchedulingAutoConfiguration.class,
      ValidationAutoConfiguration.class
    })
public class SpringApp {

  public static Consumer<Integer> exit = System::exit;

  @SneakyThrows
  public static void main(String... args) {
    val app = new SpringApplication(SpringApp.class);
    app.setBannerMode(Banner.Mode.OFF);
    app.setWebApplicationType(NONE);
    app.setLogStartupInfo(false);
    app.setRegisterShutdownHook(false);
    app.setAddCommandLineProperties(false);
    app.run(args);
  }
}
