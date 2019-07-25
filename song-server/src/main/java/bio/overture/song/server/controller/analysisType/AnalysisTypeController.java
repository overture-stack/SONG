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
package bio.overture.song.server.controller.analysisType;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.dto.schema.RegisterAnalysisTypeRequest;
import bio.overture.song.server.service.AnalysisTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toPrettyJson;
import static bio.overture.song.server.utils.CollectionUtils.isCollectionBlank;

@RestController
@RequestMapping(path = "/schemas")
@Api(tags = "Schema", description = "Get schemas used for uploads")
public class AnalysisTypeController {

  private final AnalysisTypeService analysisTypeService;

  @Autowired
  public AnalysisTypeController(@NonNull AnalysisTypeService analysisTypeService) {
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

  @ApiOperation(value = "GetLatestSchema",
      notes = "Retrieves the latest version of a schema for an analysisType" )
  @GetMapping("/analysis/{name}/latest")
  public AnalysisType getLatestSchema(@PathVariable("name") String name){
    return analysisTypeService.getLatestAnalysisType(name);
  }

  @SneakyThrows
  @ApiOperation(value = "GetMetaSchema",
      notes = "Retrieves the meta-schema used to validate AnalysisType schemas" )
  @GetMapping("/meta")
  public String getAnalysisTypeMetaSchema(){
    return toPrettyJson(readTree(analysisTypeService.getAnalysisTypeMetaSchema().toString()));
  }

  @ApiOperation(value = "RegisterAnalysisType", notes = "Registers an analysisType schema")
  @PostMapping(consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @PreAuthorize("@systemSecurity.authorize(authentication)")
  public @ResponseBody AnalysisType register(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @RequestBody RegisterAnalysisTypeRequest request) {
    return analysisTypeService.register(request.getName(), request.getSchema());
  }

  @GetMapping("/test")
  public List<String> getTest(){
    val data = generateData2(analysisTypeService, 20);
    return data.stream().map(x -> x.getName()).collect(toImmutableList());
  }

  public static List<AnalysisType> generateData2(AnalysisTypeService analysisTypeService, int repeats) {
    val randomGenerator = RandomGenerator.createRandomGenerator("temp");
    val names = IntStream.range(0, repeats)
        .boxed()
        .map(x -> "exampleAnalysisType-" + randomGenerator.generateRandomAsciiString(10))
        .collect(toImmutableList());

    return IntStream.range(0, repeats * repeats)
        .boxed()
        .map(i -> {
          val name = names.get(i % repeats);
          val schema = mapper().createObjectNode().put("$id", randomGenerator.generateRandomUUIDAsString());
          return analysisTypeService.register(name, schema);
        })
        .collect(toImmutableList());
  }




  @ApiOperation(value = "ListAnalysisTypes",
      notes = "Retrieves a list of registered analysisTypes" )
  @GetMapping
  public Page<AnalysisType> listAnalysisTypes(
      @RequestParam(value = "names", required = false) List<String> names,
      @RequestParam(value = "versions", required = false) Set<Integer> versions,
      @RequestParam(value = "hideSchema", required = false, defaultValue = "false") boolean hideSchema,
      Pageable pageable){
    Page<AnalysisType> analysisTypePage;

    if (isCollectionBlank(names)){
      analysisTypePage = analysisTypeService.listAllAnalysisTypes(pageable, hideSchema);
    }else {
      analysisTypePage = analysisTypeService.listAnalysisTypesFilterNames(names, pageable, hideSchema);
    }

    val filteredView = filterByVersion(analysisTypePage.getContent(), versions);
    return new PageImpl<>(filteredView, pageable, analysisTypePage.getTotalElements());
  }

  private static List<AnalysisType> filterByVersion(List<AnalysisType> analysisTypes,
      Set<Integer> versionsToFilter){
    return analysisTypes
        .stream()
        .filter(x-> versionsToFilter.contains(x.getVersion()))
        .collect(toImmutableList());
  }

  // GET /schemas/meta
  // POST /schemas --> register endpoint, that validates the schema against metaschema, and stores
  // POST /schemas/{analysisTypeId} --> validate a payload.
  // GET /schemas?name=<>&version=<>&limit=<>&offset=<>&sort=[ASC,DESC]&fields=[id,name,version,schema]
  //  name param filters by name
  //  version param filters by version
  //  limit  param limits output (pageable)
  //  offset param offsets the result (pageable)
  //  sort param indicates the sort direction (pageable)
  //  fields param indicates which fields are to show. The main DTO should have jackson configured to not show nulls, and the database should not retrieve more than it needs


}
