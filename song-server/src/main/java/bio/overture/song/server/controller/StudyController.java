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

import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.model.entity.composites.StudyWithDonors;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.StudyWithDonorsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/studies")
@RequiredArgsConstructor
@Api(tags = "Study", description = "Create and read studies")
public class StudyController {

  /** Dependencies */
  @Autowired private final StudyService studyService;

  @Autowired private final StudyWithDonorsService studyWithDonorsService;

  @ApiOperation(value = "GetStudy", notes = "Retrieves information for a study")
  @GetMapping("/{studyId}")
  public Study getStudy(@PathVariable("studyId") String studyId) {
    return studyService.read(studyId);
  }

  @ApiOperation(
      value = "GetEntireStudy",
      notes = "Retrieves all donor, specimen and sample data for a study")
  @GetMapping("/{studyId}/all")
  public StudyWithDonors getEntireStudy(@PathVariable("studyId") String studyId) {
    return studyWithDonorsService.readWithChildren(studyId);
  }

  @ApiOperation(value = "GetAllStudyIds", notes = "Retrieves all studyIds")
  @GetMapping("/all")
  public List<String> findAllStudies() {
    return studyService.findAllStudies();
  }

  @ApiOperation(value = "CreateStudy", notes = "Creates a new study")
  @PostMapping(
      value = "/{studyId}/",
      consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @ResponseBody
  public String saveStudy(
      @PathVariable("studyId") String studyId,
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @RequestBody Study study) {
    checkServer(
        studyId.equals(study.getStudyId()),
        getClass(),
        STUDY_ID_MISMATCH,
        "The studyId in the URL '%s' should match the studyId '%s' in the payload",
        studyId,
        study.getStudyId());
    return studyService.saveStudy(study);
  }
}
