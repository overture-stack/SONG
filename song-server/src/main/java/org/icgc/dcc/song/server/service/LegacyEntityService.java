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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.bohnman.squiggly.Squiggly;
import lombok.NoArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.LegacyEntity;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.LegacyEntityRepository;
import org.icgc.dcc.song.server.utils.ParameterChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@NoArgsConstructor
public class LegacyEntityService {

  private static final String ID = "id";
  private static final String FIELDS = "fields";
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final Set<String> NON_LEGACY_ENTITY_PARAMS = newHashSet(FIELDS, PAGE, SIZE);
  private static final String SQUIGGLY_ALL_FILTER = "**";

  /**
   * Dependencies
   */
  @Autowired
  private FileService fileService;

  @Autowired
  private AnalysisService analysisService;

  @Autowired
  private LegacyEntityRepository legacyEntityRepository;

  @Autowired
  private ParameterChecker parameterChecker;

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

  public JsonNode find( MultiValueMap<String, String> params, LegacyEntity probe, Pageable pageable) {
    val queryParameters =  extractQueryParameters(params);
    val filterParameters = extractFilterParameters(params);

    parameterChecker.checkQueryParameters(LegacyEntity.class, queryParameters);
    parameterChecker.checkFilterParameters(LegacyEntity.class, filterParameters);

    val results = legacyEntityRepository.findAll(Example.of(probe), pageable);
    val filter = buildLegacyEntityPageFilter(filterParameters);
    val mapper = Squiggly.init(JsonUtils.mapper(), filter);
    return mapper.valueToTree(results);
  }

  private Set<String> extractQueryParameters(MultiValueMap<String, String> multiValueMap){
    return multiValueMap.keySet().stream()
        .filter(k -> !NON_LEGACY_ENTITY_PARAMS.contains(k))
        .collect(toImmutableSet());
  }

  private String buildLegacyEntityPageFilter(Set<String> filteredFieldNames){
    String filter = SQUIGGLY_ALL_FILTER;
    if (!filteredFieldNames.isEmpty()){
      filter = String.format("%s,content[%s]", SQUIGGLY_ALL_FILTER, COMMA.join(filteredFieldNames));
    }
    return filter;
  }

  private Set<String> extractFilterParameters(MultiValueMap<String, String> params){
    return params.entrySet().stream()
        .filter(x -> x.getKey().equals(FIELDS))
        .map(Map.Entry::getValue)
        .flatMap(Collection::stream)
        .collect(toImmutableSet());
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
