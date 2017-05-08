package org.icgc.dcc.sodalite.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.icgc.dcc.sodalite.server.model.Analysis;
import org.icgc.dcc.sodalite.server.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}/analyses")
public class AnalysisController {

  /**
   * Dependencies
   */
  @Autowired
  private final AnalysisService analysisService;

  @PostMapping(consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @SneakyThrows
  public String createAnalysis(@PathVariable("studyId") String studyId, @RequestBody String json) {
    return analysisService.registerAnalysis(studyId, json);
  }

  @PutMapping(consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @SneakyThrows
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
