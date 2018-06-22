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
package org.icgc.dcc.song.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonDocUtils;
import org.icgc.dcc.song.core.utils.JsonSchemaUtils;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.icgc.dcc.song.server.model.entity.IdView;
import org.icgc.dcc.song.server.repository.search.IdSearchRequest;
import org.icgc.dcc.song.server.utils.ParameterChecker;
import org.icgc.dcc.song.server.validation.SchemaValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

import static org.icgc.dcc.song.server.utils.ParameterChecker.createParameterChecker;

@Slf4j
@Configuration
@Data
public class ValidationConfig {

  private static String[] schemaList = {
          "schemas/sequencingRead.json",
          "schemas/variantCall.json",
      "schemas/file_update_request.json"
  };

  @Value("${validation.delayMs:500}")
  private long validationDelay;

  @Bean
  public SchemaValidator schemaValidator() {
    return new SchemaValidator();
  }

  @Bean
  public ParameterChecker parameterChecker(){
    return createParameterChecker(
        LegacyEntity.class,
        IdSearchRequest.class,
        IdView.class,
        IdView.IdViewProjection.class
    );
  }

  @Bean
  @Profile("async-test")
  public Long validationDelayMs(){
    return getValidationDelay();
  }

  @Bean
  @SneakyThrows
  public Map<String, JsonSchema> schemaCache() {
    val cache = new HashMap<String, JsonSchema>();
    // TODO: Arrays.stream(schemaList)
    for (val schema : schemaList) {
      log.debug("Loading schema {}", schema);
      JsonNode node = JsonDocUtils.getJsonNodeFromClasspath(schema);
      cache.put(JsonSchemaUtils.getSchemaId(node), JsonSchemaUtils.getJsonSchema(node));
    }
    for (val s : cache.keySet()) {
      log.info(s);
    }
    return cache;
  }

}
