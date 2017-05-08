package org.icgc.dcc.sodalite.server.controller;

import lombok.RequiredArgsConstructor;

import org.icgc.dcc.sodalite.server.model.Donor;

import org.icgc.dcc.sodalite.server.service.DonorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}")
public class DonorController {

  @Autowired
  private final DonorService donorService;

  @PostMapping(value = "/donor", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String create(@PathVariable("study_id") String study_id, @RequestBody Donor donor) {
    return donorService.create(study_id, donor);

  }

  @GetMapping(value = "/donor/{id}")
  @ResponseBody
  public Donor read(@PathVariable("id") String id) {
    Donor d = donorService.getById(id);
    return d;
  }

  @PutMapping(value = "/donor", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String update(@PathVariable("study_id") String study_id, @RequestBody Donor donor) {
    return donorService.update(donor);
  }

  @DeleteMapping(value = "/donor/{ids}")
  public String delete(@PathVariable("ids") List<String> ids) {
    for (String id : ids) {
      donorService.delete(id);
    }
    return "OK";
  }

}
