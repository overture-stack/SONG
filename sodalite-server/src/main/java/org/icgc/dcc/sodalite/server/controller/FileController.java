package org.icgc.dcc.sodalite.server.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/studies/{study_id}")
public class FileController {

  @Autowired
  private final FileService fileService;

  @PostMapping(value = "/file", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  public String create(@PathVariable("study_id") String studyId, @RequestBody File file) {
    file.setStudyId(studyId);
    return fileService.create(file);
  }

  @GetMapping(value = "/file/{id}")
  @ResponseBody
  public File read(@PathVariable("id") String id) {
    return fileService.getById(id);
  }

  @PutMapping(value = "/file", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  public ResponseEntity<String> update(@PathVariable("study_id") String studyId, @RequestBody File file) {
    file.setStudyId(studyId);
    fileService.update(file);
    return ok("");
  }

  @DeleteMapping(value = "/file/{ids}")
  public ResponseEntity<String> delete(@PathVariable("study_id") String studyId, @PathVariable("ids") List<String> ids) {
    ids.forEach(fileService::delete);
    return ok("");
  }

}
