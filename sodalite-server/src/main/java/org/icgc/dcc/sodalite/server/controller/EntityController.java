package org.icgc.dcc.sodalite.server.controller;
import lombok.RequiredArgsConstructor;

import org.icgc.dcc.sodalite.server.model.Entity;
import org.icgc.dcc.sodalite.server.model.json.create.CreateEntityMessage;
import org.icgc.dcc.sodalite.server.model.json.update.entity.EntityUpdateMessage;
import org.icgc.dcc.sodalite.server.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study}/entities")
public class EntityController {
	
  /**
   * Dependencies
   */
  @Autowired
  private final EntityService entityService;

  @PostMapping(consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  public int createEntity(@RequestBody CreateEntityMessage message) {
    return entityService.create(message.getCreateEntity());
  }
  
  @PutMapping(consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  public int modifyEntity(@RequestBody EntityUpdateMessage message) {
    return entityService.modify(message.getEntityUpdate());
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

