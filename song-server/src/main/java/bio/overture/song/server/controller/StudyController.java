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

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.model.entity.composites.StudyWithDonors;
import bio.overture.song.server.model.legacy.EgoIsDownException;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.StudyWithDonorsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import java.util.List;

import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(path = "/studies")
@RequiredArgsConstructor
@Api(tags = "Study", description = "Create and read studies")
public class StudyController {

  /**
   * Dependencies
   */
  @Autowired
  private final StudyService studyService;

  @Autowired
  private final StudyWithDonorsService studyWithDonorsService;

  @ApiOperation(value = "GetStudy",
    notes = "Retrieves information for a study. If the study does not exist, an empty array is returned")
  @GetMapping("/{studyId}")
  public Study getStudy(
    @PathVariable("studyId") String studyId) {
    return studyService.read(studyId);
  }

  @ApiOperation(value = "GetEntireStudy", notes = "Retrieves all donor, specimen and sample data for a study")
  @GetMapping("/{studyId}/all")
  public StudyWithDonors getEntireStudy(
    @PathVariable("studyId") String studyId) {
    return studyWithDonorsService.readWithChildren(studyId);
  }

  @ApiOperation(value = "GetAllStudyIds", notes = "Retrieves all studyIds")
  @GetMapping("/all")
  public List<String> findAllStudies() {
    return studyService.findAllStudies();
  }

  @ApiOperation(value = "CreateStudy", notes = "Creates a new study")
  @PostMapping(value = "/{studyId}/", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @ResponseBody
  public ResponseEntity<String> saveStudy(@PathVariable("studyId") String studyId,
    @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
    @RequestBody Study study) {
    checkServer(studyId.equals(study.getStudyId()), getClass(), STUDY_ID_MISMATCH,
      "The studyId in the URL '%s' should match the studyId '%s' in the payload",
      studyId, study.getStudyId());
    val id = studyService.saveStudy(study);
    log.info(format("Created study '%s' with id %s", studyId, id));
    return new ResponseEntity<>(format("{\"id\": \"%s\"}",id), HttpStatus.OK);
  }


}
