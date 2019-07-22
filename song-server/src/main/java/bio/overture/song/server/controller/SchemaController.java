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

import bio.overture.song.server.model.dto.GetAnalysisTypeResponse;
import bio.overture.song.server.service.SchemaService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(path = "/schemas")
@RequiredArgsConstructor
@Api(tags = "Schema", description = "Get schemas used for uploads")
public class SchemaController {

//  @Autowired
//  private SchemaServiceOLD schemaServiceOLD;
//
//  @ApiOperation(value = "ListSchemasIds",
//      notes = "Retrieves a list of registered schema ids" )
//  @GetMapping("/list")
//  public ListSchemaIdsResponse listSchemaIds(){
//    return schemaServiceOLD.listSchemaIds();
//  }
//
//  @ApiOperation(value = "GetSchema", notes = "Retrieves the jsonSchema for a schemaId")
//  @GetMapping("/{schemaId}")
//  public GetSchemaResponse getSchema(
//      @PathVariable("schemaId") String schemaId) {
//    return schemaServiceOLD.getSchema(schemaId);
//  }

  @Autowired
  private SchemaService schemaService;

  @ApiOperation(value = "ListAnalysisTypes",
      notes = "Retrieves a list of registered analysisType names" )
  @GetMapping("/analysis")
  public Collection<String> listAnalysisTypes(){
    return schemaService.listAnalysisTypeNames();
  }

  @ApiOperation(value = "GetAnalysisTypeVersion",
      notes = "Retrieves a specific version of a schema for an analysisType" )
  @GetMapping("/analysis/{name}/{version}")
  public GetAnalysisTypeResponse getSchemaVersion(
      @PathVariable("name") String name,
      @PathVariable("version") Integer version){
    return schemaService.getSchema(name, version);
  }

  @ApiOperation(value = "GetLatestAnalysisType",
      notes = "Retrieves the latest version of a schema for an analysisType" )
  @GetMapping("/analysis/{name}")
  public GetAnalysisTypeResponse getLatestSchema(@PathVariable("name") String name){
    return schemaService.getLatestSchema(name);
  }


}
