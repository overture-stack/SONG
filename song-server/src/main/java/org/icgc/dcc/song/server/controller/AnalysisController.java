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
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.server.model.analysis.AbstractAnalysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.search.IdSearchRequest;
import org.icgc.dcc.song.server.repository.search.InfoSearchRequest;
import org.icgc.dcc.song.server.repository.search.InfoSearchResponse;
import org.icgc.dcc.song.server.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
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

import java.util.List;

import static org.icgc.dcc.song.server.repository.search.IdSearchRequest.createIdSearchRequest;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{studyId}/analysis")
@Api(tags = "Analysis", description = "Read, publish, suppress and search analyses")
public class AnalysisController {

  private static final String EXAMPLE_ANALYSIS_INFO_JSON = "{\n"
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

  /**
   * Dependencies
   */
  @Autowired
  private final AnalysisService analysisService;

  @ApiOperation(value = "GetAnalysesForStudy", notes = "Retrieve all analysis objects for a studyId")
  @GetMapping(value = "")
  public List<AbstractAnalysis> getAnalysis(
      @PathVariable("studyId") String studyId) {
    return analysisService.getAnalysis(studyId);
  }

  /**
   * [DCC-5726] - updates disabled until back propagation updates due to business key updates is implemented
   */
//  @PutMapping(consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
//  @SneakyThrows
//  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
//  public ResponseEntity<String> modifyAnalysis(@PathVariable("studyId") String studyId, @RequestBody Analysis analysis) {
//    return analysisService.updateAnalysis(studyId, analysis);
//  }

  @ApiOperation(value = "PublishAnalysis",
      notes = "Publish an analysis. This checks to see if the files associated "
      + "with the input analysisId exist in the storage server")
  @PutMapping(value="/publish/{id}")
  @SneakyThrows
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> publishAnalysis(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("id") String id) {
    return analysisService.publish(accessToken,id);
  }

  @ApiOperation(value = "SuppressAnalysis", notes = "Suppress an analysis. Used if a previously published analysis is"
      + " no longer needed. Instead of removing the analysis, it is marked as \"suppressed\"")
  @PutMapping(value="/suppress/{id}")
  @SneakyThrows
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> suppressAnalysis(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("id") String id) {
    return analysisService.suppress(id);
  }

  /***
   * Return the JSON for this analysis (it's type, details, fileIds, etc.)
   * @param id An analysis id
   * @return A JSON object representing this analysis
   */
  @ApiOperation(value = "ReadAnalysis", notes = "Retrieve the analysis object for an analysisId")
  @GetMapping(value = "/{id}")
  public AbstractAnalysis read(
      @PathVariable("studyId") String studyId,
      @PathVariable("id") String id) {
    return analysisService.deepRead(id);
  }

  /***
   * Return all of the files in the fileset for this analyis
   * @param id The analysis id
   * @return A list of all the files in this analysis analysisId's fileset.
   */
  @ApiOperation(value = "ReadAnalysisFiles", notes = "Retrieve the file objects for an analysisId")
  @GetMapping(value = "/{id}/files")
  public List<File> getFilesById(@PathVariable("id") String id) {
    return analysisService.readFiles(id);
  }


  @ApiOperation(value = "IdSearch", notes = "Search for analysis objects by specifying regex patterns for the "
      + "donorIds, sampleIds, specimenIds, or fileIds request parameters")
  @GetMapping(value = "/search/id")
  public List<AbstractAnalysis> idSearch(@PathVariable("studyId") String studyId,
      @RequestParam(value = "donorId",required = false) String donorIds,
      @RequestParam(value = "sampleId",required = false) String sampleIds,
      @RequestParam(value = "specimenId", required = false) String specimenIds,
      @RequestParam(value = "fileId", required = false) String fileIds ) {
    val request = createIdSearchRequest(donorIds, sampleIds, specimenIds, fileIds);
    return analysisService.idSearch(studyId, request);
  }

  @ApiOperation(value = "IdSearch", notes = "Search for analysis objects by specifying an IdSearchRequest" )
  @PostMapping(value = "/search/id", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public List<AbstractAnalysis> idSearch(@PathVariable("studyId") String studyId, @RequestBody IdSearchRequest request) {
    return analysisService.idSearch(studyId, request);
  }

  @ApiOperation(value = "InfoSearch", notes = "Retrieve analysis objects by searching for key-value "
      + "terms specifying the analysis info field. ",hidden = true)
  @GetMapping(value = "/search/info")
  public List<InfoSearchResponse> search(@PathVariable("studyId") String studyId,
      @RequestParam(value = "includeInfo")
          @ApiParam(value = "When true, includes the info field in the response, otherwise it is excluded"
              + "analysisId", required = true)
          boolean includeInfo,
      @RequestParam
      @ApiParam(value = "A search terms has the format {key}={value}, where key is a non-whitespace word, and value is"
          + " a regex pattern. Multiple search terms must be joined by an '&'", required = true)
          MultiValueMap<String, String> searchTerms ) {
    searchTerms.remove("includeInfo"); //Always added to map, but is redundant
    return analysisService.infoSearch(studyId,includeInfo, searchTerms);
  }

  @ApiOperation(value = "InfoSearch", notes = "Retrieve analysis objects by specifying an InfoSearchRequest and "
      + "searching the info field of all analyses for a study. The effective query is the logical AND of all search "
      + "term queries. Child keys are accessed using the '.' character. "
      + "For instance, if the analysis has the data: \n"
      + EXAMPLE_ANALYSIS_INFO_JSON
      + "\n then to search by 'eye_colour', the key of the search term would be "
      + "\n 'extra_donor_info.physical.eye_colour'")
  @PostMapping(value = "/search/info")
  public List<InfoSearchResponse> search(@PathVariable("studyId") String studyId,
      @RequestBody InfoSearchRequest infoSearchRequest){
    return analysisService.infoSearch(studyId, infoSearchRequest);
  }

}
