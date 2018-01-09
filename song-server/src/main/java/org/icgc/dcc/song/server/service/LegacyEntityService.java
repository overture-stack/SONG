package org.icgc.dcc.song.server.service;

import lombok.NoArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.model.LegacyEntity;
import org.icgc.dcc.song.server.model.entity.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@NoArgsConstructor
public class LegacyEntityService {

  private static final String ID = "id";

  /**
   * Dependencies
   */
  @Autowired
  private FileService fileService;

  @Autowired
  private AnalysisService analysisService;

  public LegacyEntity getEntity( String id) {
    val entity = fileService.read(id);
    checkServer(!isNull(entity),this.getClass(), FILE_NOT_FOUND,
        "The File with id '%s' was not found", id);
    return convert(entity);
  }

  public Page<LegacyEntity> getEntitiesByGnosId(String gnosId,
      int pageSize, int pageNum ) {
    val skipSize = pageSize*pageNum;
    val files = analysisService.readFiles(gnosId);
    val entities = files.stream()
        .map(LegacyEntityService::convert)
        .skip(skipSize)
        .limit(pageSize)
        .collect(toImmutableList());
    val sort = new Sort(new Order(ASC, ID).ignoreCase().nullsNative());
    val pageRequest = new PageRequest(pageNum,pageSize, sort);
    return new PageImpl<>(entities,pageRequest, files.size());
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
