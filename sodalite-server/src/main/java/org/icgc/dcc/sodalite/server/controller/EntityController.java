package org.icgc.dcc.sodalite.server.controller;

import lombok.RequiredArgsConstructor;

import org.icgc.dcc.sodalite.server.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}")
public class EntityController {

  /**
   * Dependencies
   */
  @Autowired
  private final EntityService entityService;

  @PostMapping(value = "/entities", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String create(@PathVariable("study_id") String study_id, @RequestBody String json) {
    return entityService.create(study_id, json);
  }

  @GetMapping(value = "/entities/{id}")
  public String read(@PathVariable("id") String id) {
    return entityService.getEntityById(id);
  }

  @PutMapping(value = "/entities", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String update(@PathVariable("study_id") String study_id, @RequestBody String json) {
    return entityService.update(study_id, json);
  }

  @DeleteMapping(value = "/entities/{ids}")
  public String delete(@PathVariable("ids") List<String> ids) {
    return entityService.delete(ids);
  }

}
