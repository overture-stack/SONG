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

import bio.overture.song.server.service.SchemaServiceEXP;
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

import java.util.Set;

@RestController
@RequestMapping(path = "/special/schema")
@RequiredArgsConstructor
@Api(tags = "SpecialSchema", description = "Get schemas used for uploads")
public class SpecialSchemaController {

  @Autowired
  private SchemaServiceEXP schemaServiceEXP;

  @ApiOperation(value = "GetDefinitions",
      notes = "Retrieves a list of registered analysis types" )
  @GetMapping("/definitions")
  public JsonNode getDefinitions(){
    return schemaServiceEXP.getDefinitions();
  }

  @ApiOperation(value = "GetAnalysisTypes",
      notes = "Retrieves a list of registered analysis types" )
  @GetMapping("/list")
  public Set<String> listAnalysisTypes(){
    return schemaServiceEXP.listAnalysisTypes();
  }

  @ApiOperation(value = "GetSchema", notes = "Retrieves the jsonSchema for a schemaId")
  @GetMapping("/{analysisType}")
  public JsonNode getAnalysisSchema(
      @PathVariable("analysisType") String analysisType) {
    return schemaServiceEXP.resolveAnalysisTypeJsonSchema(analysisType);
  }

  @ApiOperation(value = "RegisterAnalysis", notes = "Registers the experiment portion of an analysis")
  @PostMapping("/analysis/register")
  public boolean registerAnalysisType(@RequestBody JsonNode request){
    schemaServiceEXP.registerAnalysis(request);
    return true;
  }

  @ApiOperation(value = "ValidateAnalysis", notes = "Checks analysis payload is correct")
  @PostMapping("/analysis/validate")
  public boolean validatePayload(@RequestBody JsonNode payload){
    schemaServiceEXP.validatePayload(payload);
    return true;
  }

}
