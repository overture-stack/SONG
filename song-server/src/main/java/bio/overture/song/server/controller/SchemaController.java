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
package bio.overture.song.server.controller;

import bio.overture.song.server.model.dto.schema.GetSchemaResponse;
import bio.overture.song.server.model.dto.schema.ListSchemaIdsResponse;
import bio.overture.song.server.service.SchemaService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/schema")
@RequiredArgsConstructor
@Api(tags = "Schema", description = "Get schemas used for uploads")
public class SchemaController {

  @Autowired
  private SchemaService schemaService;

  @ApiOperation(value = "ListSchemasIds",
      notes = "Retrieves a list of registered schema ids" )
  @GetMapping("/list")
  public ListSchemaIdsResponse listSchemaIds(){
    return schemaService.listSchemaIds();
  }

  @ApiOperation(value = "GetSchema", notes = "Retrieves the jsonSchema for a schemaId")
  @GetMapping("/{schemaId}")
  public GetSchemaResponse getSchema(
      @PathVariable("schemaId") String schemaId) {
    return schemaService.getSchema(schemaId);
  }

  @ApiOperation(value = "RegisterAnalysis", notes = "Registers an analysis")
  @PostMapping("/analysis/register")
  public String registerSchema(@RequestBody JsonNode analysisSchema){
    return null;

  }

}
