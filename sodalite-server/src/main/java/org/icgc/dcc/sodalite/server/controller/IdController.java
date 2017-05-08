package org.icgc.dcc.sodalite.server.controller;

import org.icgc.dcc.sodalite.server.service.IdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/id")
@RequiredArgsConstructor
public class IdController {

  /**
   * Dependencies
   */
  @Autowired
  private IdService idService;

  @GetMapping(path = "/donor")
  public String generateDonorId() {
    return idService.generateDonorId();
  }

  @GetMapping(path = "/specimen")
  public String generateSpecimenId() {
    return idService.generateSpecimenId();
  }

  @GetMapping(path = "/sample")
  public String generateSampleId() {
    return idService.generateSampleId();
  }

}
