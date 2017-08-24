package org.icgc.dcc.song.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.model.MetadataAdapterModel;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/entities/")
public class EntitiesAdapterController {

  /**
   * Dependencies
   */
  @Autowired
  private final FileService fileService;

  @GetMapping(value = "{id}")
  @ResponseBody
  public MetadataAdapterModel read(@PathVariable("id") String id) {
    val file = fileService.read(id);
    return MetadataAdapterModel.builder()
        .id(file.getObjectId())
        .gnosId(file.getAnalysisId())
        .fileName(file.getFileName())
        .projectCode(file.getStudyId())
        .access("controlled")
        .build();
  }

}
