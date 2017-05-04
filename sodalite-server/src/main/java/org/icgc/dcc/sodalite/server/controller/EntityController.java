package org.icgc.dcc.sodalite.server.controller;
import lombok.RequiredArgsConstructor;

import org.icgc.dcc.sodalite.server.model.Entity;

import org.icgc.dcc.sodalite.server.service.OldEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}")
public class EntityController {
	
  /**
   * Dependencies
   */
  @Autowired
  private final OldEntityService oldEntityService;

  @PostMapping(value="/entities",consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  public String createEntity(@PathVariable("study_id") String study_id, @RequestBody String json) {
    return oldEntityService.create(study_id, json);
  }
  
  @PutMapping(value="/entities",consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  public String modifyEntity(@PathVariable("study_id") String study_id, @RequestBody String json) {
    return oldEntityService.update(study_id, json);
  }
  
  @GetMapping(value="/entities/{id}")
  public String getEntityById(@PathVariable("id") String id) {
    return oldEntityService.getEntityById(id);
  }
  

  @GetMapping(value="/entities")
  public List<Entity> getEntities(@RequestParam Map<String, String> params) {
	  return oldEntityService.getEntities(params);
  }
  
  @GetMapping(value="")
  public String getEntireStudy(@PathVariable("study_id") String id) {
	  return oldEntityService.getStudyById(id);
  }
  
  @DeleteMapping(value="/entities/{ids}")
  public String deleteEntity(@PathVariable("ids") List<String> ids) {
	  return oldEntityService.delete(ids);
  }
   
}

