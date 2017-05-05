package org.icgc.dcc.sodalite.server.controller;
import lombok.RequiredArgsConstructor;

import org.icgc.dcc.sodalite.server.model.Specimen;

import org.icgc.dcc.sodalite.server.service.SpecimenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{study_id}")
public class SpecimenController {
  @Autowired
  private final SpecimenService specimenService;
  
  @PostMapping(value="/specimen",consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  public String create(@PathVariable("study_id") String study_id, @RequestBody Specimen specimen) {
    return specimenService.create(study_id, specimen);  
  }
  
  @GetMapping(value="/specimen/{id}")
  @ResponseBody
  public Specimen read(@PathVariable("id") String id) {
	   Specimen d=specimenService.getById(id);
	   return d;
  }
  
   @PutMapping(value="/specimen",consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
   @ResponseBody
   public String update(@PathVariable("study_id") String study_id, @RequestBody Specimen specimen) {
      return specimenService.update(specimen);
   }

  @DeleteMapping(value="/specimen/{ids}")
  public String delete(@PathVariable("ids") List<String> ids) {
	  for(String id: ids) {
		  specimenService.delete(id);
	  }
	  return "OK";
  }
   
}

