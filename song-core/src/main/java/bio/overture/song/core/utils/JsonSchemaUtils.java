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
package bio.overture.song.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;

import static java.lang.String.format;
import static bio.overture.song.core.utils.JsonUtils.convertToJSONObject;

public class JsonSchemaUtils extends JsonDocUtils {

  @SneakyThrows
  public static String getSchemaId(JsonNode schemaRoot) {
    // JSON Schema id field intended to contain a URI
    val rootNode = schemaRoot.path("id");
    if (rootNode.isMissingNode()) {
      throw new IllegalArgumentException("Invalid JSON Schema found: schema missing mandatory id field");
    } else {
      return extractFromSchemaId(rootNode.asText());
    }
  }

  @SneakyThrows
  public static String extractFromSchemaId(String id) {
    int separatorPosition = id.lastIndexOf("/");
    if (separatorPosition < 0) {
      throw new IllegalArgumentException(format("Invalid JSON Schema id found: %s", id));
    } else {
      return id.substring(separatorPosition + 1);
    }
  }

  @SneakyThrows
  public static JsonSchema getJsonSchema(JsonNode node) {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    JsonSchema schema = factory.getSchema(node);
    return schema;
  }

  @SneakyThrows
  public static void validateWithSchema(@NonNull Schema schema, @NonNull JsonNode j){
    val jsonObject = convertToJSONObject(j);
    schema.validate(jsonObject);
  }
}
