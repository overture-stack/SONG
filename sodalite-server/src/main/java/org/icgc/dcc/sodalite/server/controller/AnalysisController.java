package org.icgc.dcc.sodalite.server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}/analysis")
public class AnalysisController {

  /**
   * Dependencies
   */
  // @Autowired
  // private final AnalysisService analysisService;
  //
  // @PutMapping(consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  // @ResponseBody
  // @SneakyThrows
  // @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  // public String modifyAnalysis(@PathVariable("study_id") String studyId, @RequestBody String json) {
  // return analysisService.updateAnalysis(studyId, json);
  // }
  //
  // @GetMapping(value = "/{id}")
  // public Analysis GetAnalysisyById(@PathVariable("id") String id) {
  // return analysisService.getAnalysisById(id);
  // }
  //
  // @GetMapping(value = "")
  // public List<Analysis> getAnalyses(@RequestParam Map<String, String> params) {
  // return analysisService.getAnalyses(params);
  // }

}
