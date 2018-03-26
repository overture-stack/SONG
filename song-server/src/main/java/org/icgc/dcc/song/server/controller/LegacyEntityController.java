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

package org.icgc.dcc.song.server.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.model.LegacyEntity;
import org.icgc.dcc.song.server.service.LegacyEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@RequestMapping("/entities")
@Api(tags = "Legacy Entity", description = "Legacy support for dcc-metadata")
public class LegacyEntityController {

  /**
   * Dependencies
   */
  @Autowired
  private final LegacyEntityService legacyEntityService;

  @ApiOperation(value = "ReadLegacyEntity", notes = "Read entity data for a legacy entity id")
  @GetMapping(value = "/{id}")
  @ResponseBody
  public ResponseEntity<LegacyEntity> read(@PathVariable("id") String id) {
    return ok(legacyEntityService.getEntity(id));
  }

  @ApiOperation(value = "ReadLegacyEntitiesByGnosId", notes = "Page through LegacyEntity data for a gnosId")
  @GetMapping
  @ResponseBody
  public ResponseEntity<Page<LegacyEntity>> readGnosId(
      @RequestParam(value = "gnosId") String gnosId,
      @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
      @RequestParam(value = "page", required = false, defaultValue = "0") int page ) {
    return ok(legacyEntityService.getEntitiesByGnosId(gnosId,size,page));
  }

}
