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

import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.service.AnalysisTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import static bio.overture.song.core.utils.JsonUtils.toPrettyJson;

@RestController
@RequestMapping(path = "/schemas")
@Api(tags = "Schema", description = "Get schemas used for uploads")
public class SchemaController {

  private final AnalysisTypeService analysisTypeService;

  @Autowired
  public SchemaController(@NonNull AnalysisTypeService analysisTypeService) {
    this.analysisTypeService = analysisTypeService;
  }

  @ApiOperation(value = "ListAnalysisTypes",
      notes = "Retrieves a list of registered analysisType names" )
  @GetMapping("/analysis")
  public Collection<String> listAnalysisTypes(){
    return analysisTypeService.listAnalysisTypeNames();
  }

  @ApiOperation(value = "GetAnalysisTypeVersion",
      notes = "Retrieves a specific version of a schema for an analysisType" )
  @GetMapping("/analysis/{name}/{version}")
  public AnalysisType getAnalysisTypeVersion(
      @PathVariable("name") String name,
      @PathVariable("version") Integer version){
    return analysisTypeService.getAnalysisType(name, version);
  }

  @ApiOperation(value = "GetLatestAnalysisType",
      notes = "Retrieves the latest version of a schema for an analysisType" )
  @GetMapping("/analysis/{name}/latest")
  public AnalysisType getLatestSchema(@PathVariable("name") String name){
    return analysisTypeService.getLatestAnalysisType(name);
  }

  @ApiOperation(value = "GetAnalysisTypeMetaSchema",
      notes = "Retrieves the meta-schema used to validate AnalysisType schemas" )
  @GetMapping("/analysis/meta")
  public String getAnalysisTypeMetaSchema(){
    return toPrettyJson(analysisTypeService.getAnalysisTypeMetaSchemaJson());
  }

}
