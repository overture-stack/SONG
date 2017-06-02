package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Sample;
import org.icgc.dcc.sodalite.server.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
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
import lombok.val;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{studyId}")
public class SampleController {

  /**
   * Dependencies
   */
  @Autowired
  private final SampleService sampleService;

  @PostMapping(value = "/sample", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String create(@PathVariable("studyId") String studyId, @RequestBody Sample sample) {
    val specimenId = sample.getSpecimenId();
    return sampleService.create(specimenId, sample);
  }

  @GetMapping(value = "/sample/{id}")
  @ResponseBody
  public Sample read(@PathVariable("id") String id) {
    return sampleService.read(id);
  }

  @PutMapping(value = "/sample", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String update(@PathVariable("studyId") String studyId, @RequestBody Sample sample) {
    return sampleService.update(sample);
  }

  @DeleteMapping(value = "/sample/{ids}")
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String delete(@PathVariable("studyId") String studyId, @PathVariable("ids") List<String> ids) {
    ids.forEach(sampleService::delete);
    return "OK";
  }

}
