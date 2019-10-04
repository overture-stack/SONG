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

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import bio.overture.song.core.model.FileUpdateRequest;
import bio.overture.song.core.model.FileUpdateResponse;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.service.FileModificationService;
import bio.overture.song.server.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{studyId}")
@Api(tags = "File", description = "Read and delete files")
public class FileController {

  /** Dependencies */
  @Autowired private final FileService fileService;

  @Autowired private final FileModificationService fileModificationService;

  @ApiOperation(value = "ReadFile", notes = "Retrieves file data for a fileId")
  @GetMapping(value = "/files/{id}")
  @ResponseBody
  public FileEntity read(@PathVariable("studyId") String studyId, @PathVariable("id") String id) {
    return fileService.securedRead(studyId, id);
  }

  /**
   * [DCC-5726] - updates disabled until back propagation updates due to business key updates is
   * implemented
   */
  @ApiOperation(value = "UpdateFile", notes = "Updates file data for a fileId")
  @PutMapping(value = "/files/{id}")
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @Transactional
  public FileUpdateResponse update(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("id") String id,
      @ApiParam(value = "File data to update", required = true) @RequestBody
          FileUpdateRequest fileUpdateRequest) {
    return fileModificationService.securedFileWithAnalysisUpdate(studyId, id, fileUpdateRequest);
  }

  @ApiOperation(value = "DeleteFiles", notes = "Deletes file data for fileIds")
  @DeleteMapping(value = "/files/{ids}")
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @Transactional
  public String delete(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("ids") @ApiParam(value = "Comma separated list of fileIds", required = true)
          List<String> ids) {
    return fileService.securedDelete(studyId, ids);
  }
}
