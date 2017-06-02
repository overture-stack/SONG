package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.Specimen;
import org.icgc.dcc.sodalite.server.service.SpecimenService;
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
public class SpecimenController {

  /**
   * Dependencies
   */
  @Autowired
  private final SpecimenService specimenService;

  @PostMapping(value = "/specimen", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String create(@PathVariable("studyId") String studyId, @RequestBody Specimen specimen) {
    val donorId = specimen.getDonorId();
    return specimenService.create(donorId, specimen);
  }

  @GetMapping(value = "/specimen/{id}")
  @ResponseBody
  public Specimen read(@PathVariable("id") String id) {
    return specimenService.read(id);
  }

  @PutMapping(value = "/specimen", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String update(@PathVariable("studyId") String studyId, @RequestBody Specimen specimen) {
    return specimenService.update(specimen);
  }

  @DeleteMapping(value = "/specimen/{ids}")
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String delete(@PathVariable("studyId") String studyId, @PathVariable("ids") List<String> ids) {
    ids.forEach(specimenService::delete);
    return "OK";
  }

}
