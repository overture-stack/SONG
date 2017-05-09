package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/studies/{study_id}")
public class SampleController {

  @Autowired
  private final SampleService sampleService;

  @PostMapping(value = "/sample", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String create(@PathVariable("study_id") String study_id, @RequestBody Sample sample) {
    val specimenId = (String) sample.getAdditionalProperties().get("specimenId");

    return sampleService.create(specimenId, sample);

  }

  @GetMapping(value = "/sample/{id}")
  @ResponseBody
  public Sample read(@PathVariable("id") String id) {
    return sampleService.getById(id);
  }

  @PutMapping(value = "/sample", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String update(@PathVariable("study_id") String study_id, @RequestBody Sample sample) {
    return sampleService.update(sample);
  }

  @DeleteMapping(value = "/sample/{ids}")
  public String delete(@PathVariable("ids") List<String> ids) {
    ids.forEach(sampleService::delete);
    return "OK";
  }

}
