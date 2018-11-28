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
package bio.overture.song.server.service;

import bio.overture.song.server.model.dto.schema.GetSchemaResponse;
import bio.overture.song.server.model.dto.schema.ListSchemaIdsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonDocUtils.getJsonNodeFromClasspath;

@Service
public class SchemaService {

  /**
   * Dependencies
   */
  private final Map<String, JsonNode> jsonSchemaDataMap;

  @Autowired
  public SchemaService(Map<String, String> jsonSchemaPathMap) {
    this.jsonSchemaDataMap = resolveJsonSchemaDataMap(jsonSchemaPathMap);
  }

  public ListSchemaIdsResponse listSchemaIds(){
   return ListSchemaIdsResponse.builder()
       .schemaIds(jsonSchemaDataMap.keySet())
       .build();
  }

  public GetSchemaResponse getSchema(@NonNull String schemaId){
    checkSchemaExists(schemaId);
    return GetSchemaResponse.builder()
        .schemaId(schemaId)
        .jsonSchema(jsonSchemaDataMap.get(schemaId))
        .build();
  }

  private void checkSchemaExists(String schemaId){
    checkServer(isSchemaExist(schemaId),  getClass(), SCHEMA_NOT_FOUND,
        "The schemaId '%s' does not exist", schemaId);
  }

  private boolean isSchemaExist(String schemaId){
    return jsonSchemaDataMap.containsKey(schemaId);
  }

  private static Map<String, JsonNode> resolveJsonSchemaDataMap(Map<String, String> jsonSchemaMap){
    return jsonSchemaMap.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, x -> getJsonNodeFromClasspath(x.getValue())));
  }

}
