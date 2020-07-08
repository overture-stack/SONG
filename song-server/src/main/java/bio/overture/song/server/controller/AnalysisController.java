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

import static bio.overture.song.core.utils.Separators.COMMA;
import static bio.overture.song.server.repository.search.IdSearchRequest.createIdSearchRequest;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.repository.search.IdSearchRequest;
import bio.overture.song.server.service.analysis.AnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/studies/{studyId}/analysis")
@Api(
    tags = "Analysis",
    description = "Create, Read, Update, publish, unpublish, suppress and search analyses")
public class AnalysisController {

  private static final String EXAMPLE_ANALYSIS_INFO_JSON =
      "{\n"
          + "  \"info\" : {\n"
          + "    \"extra_donor_info\" : {\n"
          + "      \"physical\" : {\n"
          + "        \"eye_colour\" : \"blue\",\n"
          + "        \"hair_colour\" : \"brown\"\n"
          + "      },\n"
          + "      \"occupation\" : \"engineer\"\n"
          + "    }\n"
          + "  }\n"
          + "}";

  /** Dependencies */
  private final AnalysisService analysisService;

  @Autowired
  public AnalysisController(@NonNull AnalysisService analysisService) {
    this.analysisService = analysisService;
  }

  @ApiOperation(
      value = "GetAnalysesForStudy",
      notes = "Retrieve all analysis objects for a studyId")
  @GetMapping(value = "")
  public List<Analysis> getAnalysis(
      @PathVariable("studyId") String studyId,
      @ApiParam(value = "Non-empty comma separated list of analysis states to filter by")
          @RequestParam(value = "analysisStates", defaultValue = "PUBLISHED", required = false)
          String analysisStates) {
    return analysisService.getAnalysis(studyId, ImmutableSet.copyOf(COMMA.split(analysisStates)));
  }

  /** [DCC-5726] - non-dynamic updates disabled until hibernate is properly integrated */
  @ApiOperation(value = "UpdateAnalysis", notes = "Update dynamic-data for for an analysis")
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @PutMapping(
      value = "/{analysisId}",
      consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  public void updateAnalysis(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("analysisId") String analysisId,
      @RequestBody JsonNode updateAnalysisRequest) {
    analysisService.updateAnalysis(studyId, analysisId, updateAnalysisRequest);
  }

  @ApiOperation(
      value = "PublishAnalysis",
      notes =
          "Publish an analysis. This checks to see if the files associated "
              + "with the input analysisId exist in the storage server")
  @PutMapping(value = "/publish/{id}")
  @SneakyThrows
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> publishAnalysis(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("id") String id,
      @ApiParam(value = "Ignores files that have an undefined MD5 checksum when publishing")
          @RequestParam(value = "ignoreUndefinedMd5", defaultValue = "false", required = false)
          boolean ignoreUndefinedMd5) {
    return analysisService.publish(studyId, id, ignoreUndefinedMd5);
  }

  @ApiOperation(
      value = "UnpublishAnalysis",
      notes = "Unpublish an analysis. Set the analysis status to unpublished")
  @PutMapping(value = "/unpublish/{id}")
  @SneakyThrows
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> unpublishAnalysis(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("id") String id) {
    return analysisService.unpublish(studyId, id);
  }

  @ApiOperation(
      value = "SuppressAnalysis",
      notes =
          "Suppress an analysis. Used if a previously published analysis is"
              + " no longer needed. Instead of removing the analysis, it is marked as \"suppressed\"")
  @PutMapping(value = "/suppress/{id}")
  @SneakyThrows
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> suppressAnalysis(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("id") String id) {
    return analysisService.suppress(studyId, id);
  }

  /**
   * * Return the JSON for this analysis (it's type, details, fileIds, etc.)
   *
   * @param id An analysis id
   * @return A JSON object representing this analysis
   */
  @ApiOperation(value = "ReadAnalysis", notes = "Retrieve the analysis object for an analysisId")
  @GetMapping(value = "/{id}")
  public Analysis read(@PathVariable("studyId") String studyId, @PathVariable("id") String id) {
    return analysisService.securedDeepRead(studyId, id);
  }

  /**
   * * Return all of the files in the fileset for this analysis
   *
   * @param id The analysis id
   * @return A list of all the files in this analysis analysisId's fileset.
   */
  @ApiOperation(value = "ReadAnalysisFiles", notes = "Retrieve the file objects for an analysisId")
  @GetMapping(value = "/{id}/files")
  public List<FileEntity> getFilesById(
      @PathVariable("studyId") String studyId, @PathVariable("id") String id) {
    return analysisService.securedReadFiles(studyId, id);
  }

  @ApiOperation(
      value = "IdSearch",
      notes =
          "Search for analysis objects by specifying regex patterns for the "
              + "donorIds, sampleIds, specimenIds, or fileIds request parameters")
  @GetMapping(value = "/search/id")
  public List<Analysis> idSearch(
      @PathVariable("studyId") String studyId,
      @RequestParam(value = "donorId", required = false) String donorIds,
      @RequestParam(value = "sampleId", required = false) String sampleIds,
      @RequestParam(value = "specimenId", required = false) String specimenIds,
      @RequestParam(value = "fileId", required = false) String fileIds) {
    val request = createIdSearchRequest(donorIds, sampleIds, specimenIds, fileIds);
    return analysisService.idSearch(studyId, request);
  }

  @ApiOperation(
      value = "IdSearch",
      notes = "Search for analysis objects by specifying an IdSearchRequest")
  @PostMapping(
      value = "/search/id",
      consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  public List<Analysis> idSearch(
      @PathVariable("studyId") String studyId, @RequestBody IdSearchRequest request) {
    return analysisService.idSearch(studyId, request);
  }
}
