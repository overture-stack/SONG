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

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.service.DonorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{studyId}")
@Api(tags = "Donor", description = "Read and delete donors")
public class DonorController {

  /** Dependencies */
  @Autowired private final DonorService donorService;

  @ApiOperation(value = "ReadDonor", notes = "Retrieves donor data for a donorId")
  @GetMapping(value = "/donors/{id}")
  @ResponseBody
  public Donor read(@PathVariable("studyId") String studyId, @PathVariable("id") String id) {
    return donorService.securedRead(studyId, id);
  }

  /**
   * [DCC-5726] - updates disabled until back propagation updates due to business key updates is
   * implemented
   */
  //  @PutMapping(value = "/donors/{id}", consumes = { APPLICATION_JSON_VALUE,
  // APPLICATION_JSON_UTF8_VALUE })
  //  @ResponseBody
  //  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  //  public String update(@PathVariable("study") String studyId, @PathVariable("id") String id,
  // @RequestBody Donor donor) {
  //    // TODO: [DCC-5642] Add checkRequest between path ID and Entity's ID
  //    return donorService.update(donor);
  //  }

  @ApiOperation(
      value = "DeleteDonors",
      notes = "Deletes donor data and all dependent specimens and samples for donorIds")
  @DeleteMapping(value = "/donors/{ids}")
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String delete(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("ids") @ApiParam(value = "Comma separated list of donorIds", required = true)
          List<String> ids) {
    return donorService.securedDelete(studyId, ids);
  }
}
