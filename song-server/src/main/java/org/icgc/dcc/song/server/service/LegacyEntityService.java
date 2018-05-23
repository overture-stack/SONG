/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.icgc.dcc.song.server.service;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.server.model.LegacyEntity;
import org.icgc.dcc.song.server.model.entity.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.server.model.enums.ModelAttributeNames.OBJECT_ID;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@NoArgsConstructor
public class LegacyEntityService {

  private static final String ID = "id";
  private static final String GNOS_ID = "gnosId";

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

  public Page<LegacyEntity> find(
      @NonNull MultiValueMap<String, String> multiValueMap,
      @NonNull Pageable pageable ) {
    val gnosIdResult = extractGnosId(multiValueMap);
    if (gnosIdResult.isPresent()){
      return getEntitiesByGnosId(gnosIdResult.get(), pageable.getPageNumber(), pageable.getPageSize());
    } else {
      return getAllEntities(pageable);
    }
  }

  public Page<LegacyEntity> getAllEntities(Pageable pageable) {
    val entities = fileService.findAll(buildFilePageable(pageable))
        .stream()
        .map(LegacyEntityService::convert)
        .collect(toImmutableList());
    return new PageImpl<>(entities, buildLegacyEntityPageRequest(pageable), entities.size());
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

  private Optional<String> extractGnosId(MultiValueMap<String, String> multiValueMap){
    return Optional.ofNullable(multiValueMap.getFirst(GNOS_ID));
  }

  private static Pageable buildFilePageable(Pageable pageable){
    return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(OBJECT_ID));
  }

  private static PageRequest buildLegacyEntityPageRequest(Pageable pageable){
    val legacyEntitySort = Sort.by(
        Order.asc(ID)
            .ignoreCase()
            .nullsNative()
    );
    return PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        legacyEntitySort);
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
