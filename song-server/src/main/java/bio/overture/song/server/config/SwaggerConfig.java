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

package bio.overture.song.server.config;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

  @Value("${server.version}")
  private String serverVersion;

  @Value("${swagger.alternateUrl:/swagger}")
  @Getter
  private String alternateSwaggerUrl;

  // default is empty
  @Value("${swagger.host:}")
  private String swaggerHost;

  // default is empty
  @Value("${swagger.basePath:}")
  private String basePath;

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo())
        .select()
        .apis(basePackage("bio.overture.song.server.controller"))
        .build()
        .host(swaggerHost)
        .pathProvider(
            new RelativePathProvider(null) {
              @Override
              public String getApplicationBasePath() {
                return basePath;
              }
            });
  }

  @Bean
  UiConfiguration uiConfig() {
    return new UiConfiguration(
        "validatorUrl",
        "none",
        "alpha",
        "schema",
        UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS,
        false,
        true,
        60000L);
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("Song API")
        .description(
            "Song API reference for developers. SONG is an open source system for validating and "
                + "tracking metadata about raw data submissions, assigning "
                + "identifiers to entities of interest, and managing the state "
                + "of the raw data with regards to publication and access")
        .version(serverVersion)
        .build();
  }
}
