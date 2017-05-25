package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.analysis.Analysis;
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
@RequestMapping("/studies/{study_id}/analysis")
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
  public String modifyAnalysis(@PathVariable("study_id") String studyId, @RequestBody String json) {
    return analysisService.updateAnalysis(studyId, json);
  }

  @GetMapping(value = "/{id}")
  public List<Analysis> GetAnalysisyById(@PathVariable("id") String id) {
    return analysisService.getAnalysisById(id);
  }

  @GetMapping(value = "")
  public List<Analysis> getAnalyses(@RequestParam Map<String, String> params) {
    return analysisService.getAnalyses(params);
  }

}
