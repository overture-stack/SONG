package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}")
public class SampleController {

  @Autowired
  private final SampleService sampleService;

  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @PostMapping(value = "/sample", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String create(@PathVariable("study_id") String studyId, @RequestBody Sample sample) {
    sample.setStudyId(studyId);
    return sampleService.create(sample);
  }

  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @GetMapping(value = "/sample/{id}")
  @ResponseBody
  public Sample read(@PathVariable("id") String id) {
    return sampleService.getById(id);
  }

  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @PutMapping(value = "/sample", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  public ResponseEntity<String> update(@PathVariable("study_id") String studyId, @RequestBody Sample sample) {
    sample.setStudyId(studyId);
    sampleService.update(sample);
    return ok("");
  }

  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @DeleteMapping(value = "/sample/{ids}")
  public ResponseEntity<String> delete(@PathVariable("ids") List<String> ids) {
    ids.forEach(sampleService::delete);
    return ok("");
  }

}
