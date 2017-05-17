package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Specimen;
import org.icgc.dcc.sodalite.server.service.SpecimenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.ResponseEntity.ok;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}")
public class SpecimenController {

  @Autowired
  private final SpecimenService specimenService;

  @PostMapping(value = "/specimen", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String create(@PathVariable("study_id") String studyId, @RequestBody Specimen specimen) {
    specimen.setStudyId(studyId);
    return specimenService.create(specimen);
  }

  @GetMapping(value = "/specimen/{id}")
  @ResponseBody
  public Specimen read(@PathVariable("id") String id) {
    return specimenService.getById(id);
  }

  @PutMapping(value = "/specimen", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  public ResponseEntity<String> update(@PathVariable("study_id") String studyId, @RequestBody Specimen specimen) {
    specimen.setStudyId(studyId);
    specimenService.update(specimen);
    return ok("");
  }

  @DeleteMapping(value = "/specimen/{ids}")
  public ResponseEntity<String> delete(@PathVariable("study_id") String studyId, @PathVariable("ids") List<String> ids) {
    ids.forEach(specimenService::delete);
    return ok("");
  }

}
