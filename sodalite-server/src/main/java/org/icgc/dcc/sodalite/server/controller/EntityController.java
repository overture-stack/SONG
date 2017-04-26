package org.icgc.dcc.sodalite.server.controller;
import lombok.RequiredArgsConstructor;

import org.icgc.dcc.sodalite.server.model.Entity;
import org.icgc.dcc.sodalite.server.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}/entities")
public class EntityController {
	
  /**
   * Dependencies
   */
  @Autowired
  private final EntityService entityService;

  @PostMapping(consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  public String createEntity(@PathVariable("study_id") String study_id, @RequestBody String json) {
    return entityService.create(study_id, json);
  }
  
  @PutMapping(consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  public String modifyEntity(@PathVariable("study_id") String study_id, String json) {
    return entityService.update(study_id, json);
  }
  
  
  @GetMapping(value="/{id}")
  public List<Entity> getEntityById(@PathVariable("id") String id) {
    return entityService.getEntityById(id);
  }

  @GetMapping(value="")
  public List<Entity> getEntities(@RequestParam Map<String, String> params) {
	  return entityService.getEntities(params);
  }
  
  @DeleteMapping(value="/{ids}")
  public int deleteEntity(@PathVariable("ids") List<String> ids) {
	  return entityService.deleteEntity(ids);
  }
   
}

