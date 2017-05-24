package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.service.DonorService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{studyId}")
public class DonorController {

  /**
   * Dependencies
   */
  @Autowired
  private final DonorService donorService;

  @PostMapping(value = "/donor", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String create(@PathVariable("study_id") String studyId, @RequestBody Donor donor) {
    return donorService.create(studyId, donor);

  }

  @GetMapping(value = "/donor/{id}")
  @ResponseBody
  public Donor read(@PathVariable("studyId") String studyId, @PathVariable("id") String id) {
    return donorService.getById(studyId, id);
  }

  @PutMapping(value = "/donor", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String update(@PathVariable("studyId") String studyId, @RequestBody Donor donor) {
    return donorService.update(studyId, donor);
  }

  @DeleteMapping(value = "/donor/{ids}")
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String delete(@PathVariable("studyId") String studyId, @PathVariable("ids") List<String> ids) {
    ids.forEach(id -> donorService.delete(studyId, id));
    return "OK";
  }

}
