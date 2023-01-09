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
package bio.overture.song.server.controller.analysisType;

import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toPrettyJson;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_LIMIT;
import static bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.DEFAULT_OFFSET;
import static bio.overture.song.server.model.enums.ModelAttributeNames.LIMIT;
import static bio.overture.song.server.model.enums.ModelAttributeNames.OFFSET;
import static bio.overture.song.server.model.enums.ModelAttributeNames.SORT;
import static bio.overture.song.server.model.enums.ModelAttributeNames.SORTORDER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import bio.overture.song.core.model.AnalysisType;
import bio.overture.song.core.model.PageDTO;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.service.AnalysisTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/schemas")
@Api(tags = "Schema", description = "Get schemas used for uploads")
public class AnalysisTypeController {

  public static final String REGISTRATION = "registration";

  private final AnalysisTypeService analysisTypeService;

  @Autowired
  public AnalysisTypeController(@NonNull AnalysisTypeService analysisTypeService) {
    this.analysisTypeService = analysisTypeService;
  }

  @GetMapping("/{name}")
  @ApiOperation(
      value = "GetAnalysisTypeVersion",
      notes = "Retrieves the latest version of a schema for an analysisType")
  public AnalysisType getAnalysisTypeVersion(
      @ApiParam(value = "The name of an analysisType", type = "string", required = true)
          @PathVariable(value = "name", required = true)
          String name,
      @ApiParam(
              value = "Only retrieve the unrendered schema that was initially registered",
              type = "boolean",
              defaultValue = "false",
              required = false)
          @RequestParam(value = "unrenderedOnly", required = false, defaultValue = "false")
          boolean unrenderedOnly,
      @ApiParam(
              value = "Optionally, retrieve a specific version of the analysisType",
              type = "integer",
              required = false)
          @RequestParam(value = "version", required = false)
          Integer version) {
    return analysisTypeService.getAnalysisType(name, version, unrenderedOnly);
  }

  @SneakyThrows
  @GetMapping("/" + REGISTRATION)
  @ApiOperation(
      value = "GetRegistrationSchema",
      notes = "Retrieves the meta-schema used to validate AnalysisType schemas during registration")
  public String getAnalysisTypeRegistrationSchema() {
    return toPrettyJson(
        readTree(analysisTypeService.getAnalysisTypeRegistrationSchema().toString()));
  }

  @PreAuthorize("@systemSecurity.authorize(authentication)")
  @PostMapping(consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ApiOperation(value = "RegisterAnalysisType", notes = "Registers an analysisType schema")
  public @ResponseBody AnalysisType register(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @RequestBody RegisterAnalysisTypeRequest request) {
    return analysisTypeService.register(request.getName(), request.getSchema());
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        name = OFFSET,
        required = false,
        dataType = "integer",
        paramType = "query",
        defaultValue = "" + DEFAULT_OFFSET,
        value = "Index of first result to retrieve"),
    @ApiImplicitParam(
        name = LIMIT,
        required = false,
        dataType = "integer",
        paramType = "query",
        defaultValue = "" + DEFAULT_LIMIT,
        value = "Number of results to retrieve"),
    @ApiImplicitParam(
        name = SORT,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Comma separated fields to sort on"),
    @ApiImplicitParam(
        name = SORTORDER,
        required = false,
        dataType = "string",
        paramType = "query",
        value = "Sorting order: ASC|DESC. Default order: DESC")
  })
  @ApiOperation(value = "ListAnalysisTypes", notes = "Retrieves a list of registered analysisTypes")
  @GetMapping
  public PageDTO<AnalysisType> listAnalysisTypes(
      @ApiParam(
              value = "Comma separated list of names",
              required = false,
              example = "name1,name4,name100",
              type = "string")
          @RequestParam(value = "names", required = false)
          List<String> names,
      @ApiParam(
              value = "Comma separated list of versions",
              required = false,
              example = "4,5,9",
              type = "string")
          @RequestParam(value = "versions", required = false)
          Set<Integer> versions,
      @ApiParam(
              value = "Hide the schema field from the response",
              type = "boolean",
              required = false)
          @RequestParam(value = "hideSchema", required = false, defaultValue = "false")
          boolean hideSchema,
      @ApiParam(
              value = "Only retrieve the unrendered schema that was initially registered",
              type = "boolean",
              defaultValue = "false",
              required = false)
          @RequestParam(value = "unrenderedOnly", required = false, defaultValue = "false")
          boolean unrenderedOnly,
      Pageable pageable) {
    return analysisTypeService.listAnalysisTypes(
        names, versions, pageable, hideSchema, unrenderedOnly);
  }
}
