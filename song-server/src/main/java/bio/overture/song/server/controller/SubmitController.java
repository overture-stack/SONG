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

import bio.overture.song.core.model.SubmitResponse;
import bio.overture.song.server.service.SubmitService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(path = "/submit")
@RequiredArgsConstructor
@Api(tags = "Submit", description = "Submit and validate json metadata")
public class SubmitController {

  /** Dependencies */
  @Autowired private final SubmitService submitService;

  @ApiOperation(value = "Submit", notes = "Synchronously submit a json payload")
  @PostMapping(
      value = "/{studyId}",
      consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public SubmitResponse submit(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @RequestBody @Valid String json_payload) {
    return submitService.submit(studyId, json_payload);
  }
}
