package org.icgc.dcc.song.importer.config;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.UrlResource;

import java.nio.file.Paths;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@ConfigurationProperties
public class ApplicationConfig {

  @Bean
  @SneakyThrows
  public static PropertySourcesPlaceholderConfigurer properties() {
    val propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
    val yaml = new YamlPropertiesFactoryBean();
    val userDir = Paths.get(System.getProperty("user.dir"));
    if (userDir.equals()){


    } else {

    }
    val applicationPath = userDir.resolve("song-server/src/main/resources/application.yml").toAbsolutePath();
    val resource  = new UrlResource(applicationPath.toUri().toURL());
    yaml.setResources(resource);

    propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
    return propertySourcesPlaceholderConfigurer;
  }

}
