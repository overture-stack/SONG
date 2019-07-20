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

import bio.overture.song.server.model.Upload;
import bio.overture.song.server.model.dto.RegisterAnalysisTypeResponse;
import bio.overture.song.server.service.UploadService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

import javax.validation.Valid;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(path = "/upload")
@RequiredArgsConstructor
@Api(tags = "Upload", description = "Validate, monitor and save json metadata")
public class UploadController {

  /**
   * Dependencies
   */
  @Autowired
  private final UploadService uploadService;

  @ApiOperation(value = "SyncUpload", notes = "Synchronously uploads a json payload")
  @PostMapping(value = "/{studyId}", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> syncUpload(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @RequestBody @Valid String json_payload ) {
    return uploadService.upload(studyId, json_payload, false);
  }

  @ApiOperation(value = "AsyncUpload", notes = "Asynchronously uploads a json payload")
  @PostMapping(value = "/{studyId}/async", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> asyncUpload(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @RequestBody @Valid String json_payload ) {
    return uploadService.upload(studyId, json_payload, true);
  }

  @ApiOperation(value = "GetUploadStatus", notes = "Checks the status of an upload")
  @GetMapping(value = "/{studyId}/status/{uploadId}")
  public @ResponseBody
  Upload status(@PathVariable("studyId") String studyId, @PathVariable("uploadId") String uploadId) {
    return uploadService.securedRead(studyId, uploadId);
  }

  @ApiOperation(value = "SaveUpload", notes = "Saves an upload specified by an uploadId. Also, optionally ignores "
      + "analysisId collisions")
  @PostMapping(value = "/{studyId}/save/{uploadId}")
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> save(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("uploadId") String uploadId,
      @RequestParam(value = "ignoreAnalysisIdCollisions", defaultValue = "false") boolean ignoreAnalysisIdCollisions ) {
    return uploadService.save(studyId, uploadId, ignoreAnalysisIdCollisions);
  }

  @ApiOperation(value = "RegisterAnalysisType", notes = "Registers an analysisType schema")
  @PostMapping(value = "/schema/{analysisTypeName}",
      consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  public @ResponseBody RegisterAnalysisTypeResponse save(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("analysisTypeName") String analysisTypeName,
      @RequestBody JsonNode analysisTypeSchema ) {
    return uploadService.register(analysisTypeName, analysisTypeSchema);
  }

}
