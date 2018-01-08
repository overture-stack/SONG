package org.icgc.dcc.song.server.service;

import lombok.NoArgsConstructor;
import org.icgc.dcc.song.server.model.LegacyEntity;
import org.icgc.dcc.song.server.model.entity.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

@Service
@NoArgsConstructor
public class LegacyEntityService {

  /**
   * Dependencies
   */
  @Autowired
  private FileService fileService;

  @Autowired
  private AnalysisService analysisService;

  public LegacyEntity getEntity(@PathVariable("id") String id) {
    return convert(fileService.read(id));
  }

  public List<LegacyEntity> getEntityByGnosId(@PathVariable(value = "gnosId", required = true) String gnosId,
      @PathVariable(value = "size", required = false) int size,
      @PathVariable(value = "page", required = false) int page ) {
    return analysisService.readFiles(gnosId).stream()
        .map(LegacyEntityService::convert)
        .collect(toImmutableList());
  }

  private static LegacyEntity convert(File file){
    return LegacyEntity.builder()
        .id(file.getObjectId())
        .gnosId(file.getAnalysisId())
        .fileName(file.getFileName())
        .projectCode(file.getStudyId())
        .access(file.getFileAccess())
        .build();
  }

}
