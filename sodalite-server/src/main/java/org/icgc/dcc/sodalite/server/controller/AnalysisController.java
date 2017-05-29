package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{studyId}/analysis")
public class AnalysisController {

  /**
   * Dependencies
   */
  @Autowired
  private final AnalysisService analysisService;

  @PutMapping(consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @SneakyThrows
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String modifyAnalysis(@PathVariable("studyId") String studyId, @RequestBody String json) {
    return analysisService.updateAnalysis(studyId, json);
  }

  // Return the JSON for this analysis (it's type, details, fileIds, etc.)
  @GetMapping(value = "/{id}")
  public String getAnalysisyById(@PathVariable("id") String id) {
    return analysisService.getAnalysisById(id);
  }

  // All the analysis ids for this study matching the given parameters
  @GetMapping(value = "")
  public List<String> getAnalyses(@RequestParam Map<String, String> params) {
    return analysisService.getAnalyses(params);
  }

}
