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

package bio.overture.song.server.controller;

import static org.springframework.http.ResponseEntity.ok;

import bio.overture.song.server.model.legacy.Legacy;
import bio.overture.song.server.model.legacy.LegacyDto;
import bio.overture.song.server.service.LegacyEntityService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/entities")
@Api(tags = "Legacy Entity", description = "Legacy support for dcc-metadata")
public class LegacyEntityController {

  /** Dependencies */
  @Autowired private final LegacyEntityService legacyEntityService;

  @ApiOperation(value = "ReadLegacyEntity", notes = "Read entity data for a legacy entity id")
  @GetMapping(value = "/{id}")
  @ResponseBody
  public ResponseEntity<Legacy> read(@PathVariable("id") String id) {
    return ok(legacyEntityService.getEntity(id));
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "page",
        dataType = "integer",
        paramType = "query",
        value = "Results page you want to retrieve (0..N)"),
    @ApiImplicitParam(
        name = "size",
        dataType = "integer",
        paramType = "query",
        value = "Number of records per page.")
  })
  @ApiOperation(value = "FindLegacyEntities", notes = "Page through LegacyEntity data")
  @ResponseBody
  @GetMapping
  public ResponseEntity<JsonNode> find(
      @RequestParam(required = false) MultiValueMap<String, String> fields,
      @ModelAttribute LegacyDto probe,
      @PageableDefault(sort = "id") Pageable pageable) {
    return ok(legacyEntityService.find(fields, probe, pageable));
  }
}
