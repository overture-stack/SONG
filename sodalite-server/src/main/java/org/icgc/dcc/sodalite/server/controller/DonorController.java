package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.Donor;
import org.icgc.dcc.sodalite.server.service.DonorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}")
public class DonorController {

  @Autowired
  private final DonorService donorService;

  @PostMapping(value = "/donor", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  public ResponseEntity<String> create(@PathVariable("study_id") String studyId, @RequestBody Donor donor) {
    donor.setStudyId(studyId);
    return ok(donorService.create(donor));
  }

  @GetMapping(value = "/donor/{id}")
  public ResponseEntity<Donor> read(@PathVariable("study_id") String studyId, @PathVariable("id") String id) {
    val donor = donorService.getById(id);
    if (donor != null) {
      return ok(donor);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(value = "/donor", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  public ResponseEntity<String> update(@PathVariable("study_id") String studyId, @RequestBody Donor donor) {
    donor.setStudyId(studyId);
    donorService.update(donor);
    return ok("");
  }

  @DeleteMapping(value = "/donor/{ids}")
  public ResponseEntity<String> delete(@PathVariable("study_id") String studyId, @PathVariable("ids") List<String> ids) {
    ids.forEach(id -> donorService.delete(id));
    // TODO: maybe return count of items deleted?
    return ok("");
  }

}
