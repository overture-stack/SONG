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
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.service.SpecimenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.icgc.dcc.song.core.utils.Responses.OK;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{studyId}")
@Api(tags = "Specimen", description = "Create,read and delete specimens")
public class SpecimenController {

  /**
   * Dependencies
   */
  @Autowired
  private final SpecimenService specimenService;

  @ApiOperation(value = "ReadSpecimen", notes = "Retrieves specimen data for a specimenId")
  @GetMapping(value = "/specimens/{id}")
  @ResponseBody
  public Specimen read(@PathVariable("id") String id) {
    return specimenService.read(id);
  }

  /**
   * [DCC-5726] - updates disabled until back propagation updates due to business key updates is implemented
   */
//  @PutMapping(value = "/specimens/{id}", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
//  @ResponseBody
//  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
//  public String update(@PathVariable("studyId") String studyId, @PathVariable("id") String id,
//                       @RequestBody Specimen specimen) {
//    // TODO: [DCC-5642] Add checkRequest between path ID and Entity's ID
//    return specimenService.update(specimen);
//  }

  @ApiOperation(value = "DeleteSpecimens", notes = "Deletes specimen data and all dependent samples for specimenIds")
  @DeleteMapping(value = "/specimens/{ids}")
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String delete(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("ids") @ApiParam(value = "Comma separated list of specimenIds", required = true)
      List<String> ids) {
    specimenService.delete(ids);
    return OK;
  }

}
