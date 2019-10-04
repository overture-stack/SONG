/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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
package bio.overture.song.server.validation;

import static java.lang.Thread.sleep;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Potentially extract a Validator interface if we want to pursue a Strategy pattern of multiple
 * validation rules or something
 */
@Slf4j
@Deprecated
public class SchemaValidator {

  @Autowired private Map<String, JsonSchema> schemaCache;

  @Autowired(required = false)
  private Long validationDelayMs = -1L;

  @SneakyThrows
  public ValidationResponse validate(String schemaId, JsonNode payloadRoot) {
    if (schemaCache.containsKey(schemaId)) {
      val schema = schemaCache.get(schemaId);
      val results = schema.validate(payloadRoot);
      val response = new ValidationResponse(results);
      log.info(response.getValidationErrors());

      debugDelay();

      return response;
    } else {
      // log to database
      throw new IllegalArgumentException(
          "Internal Error: could not find specified schema " + schemaId);
    }
  }

  /**
   * Creates an artificial delay for testing purposes. The validationDelayMs should be controlled
   * through the Spring "test" profile
   */
  @SneakyThrows
  private void debugDelay() {
    if (validationDelayMs > -1L) {
      log.info("Sleeping for {} ms", validationDelayMs);
      sleep(validationDelayMs);
    }
  }
}
