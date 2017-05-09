package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/studies/{study_id}")
public class FileController {

  @Autowired
  private final FileService fileService;

  @PostMapping(value = "/file", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String create(@PathVariable("study_id") String study_id, @RequestBody File file) {
    val sampleId = (String) file.getAdditionalProperties().get("sampleId");
    return fileService.create(sampleId, file);
  }

  @GetMapping(value = "/file/{id}")
  @ResponseBody
  public File read(@PathVariable("id") String id) {
    return fileService.getById(id);
  }

  @PutMapping(value = "/file", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String update(@PathVariable("study_id") String study_id, @RequestBody File file) {
    return fileService.update(file);
  }

  @DeleteMapping(value = "/file/{ids}")
  public String delete(@PathVariable("study_id") String study_id, @PathVariable("ids") List<String> ids) {
    ids.forEach(fileService::delete);
    return "OK";
  }

}
