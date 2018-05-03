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

package org.icgc.dcc.song.server.repository.search;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.Session;
import org.icgc.dcc.song.server.model.analysis.AbstractAnalysis;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.entity.IdView;
import org.icgc.dcc.song.server.model.entity.IdView.IdViewProjection;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.utils.JsonUtils.readTree;
import static org.icgc.dcc.song.server.repository.search.InfoSearchResponse.createWithInfo;
import static org.icgc.dcc.song.server.repository.search.SearchQueryBuilder.createSearchQueryBuilder;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class SearchRepository {

  private final EntityManager em;

  public List<InfoSearchResponse> infoSearch(boolean includeInfo, @NonNull Iterable<SearchTerm> searchTerms){
    val session = em.unwrap(Session.class);
    val searchQueryBuilder = createSearchQueryBuilder(includeInfo);
    searchTerms.forEach(searchQueryBuilder::add);

    Object output = session.createNativeQuery(searchQueryBuilder.build()).getResultList();
    if (includeInfo){
      return ((List<Object[]>)output).stream()
          .map(SearchRepository::mapWithInfo)
          .collect(toImmutableList());
    } else {
      return ((List<String>)output).stream()
          .map(InfoSearchResponse::createWithoutInfo)
          .collect(toImmutableList());
    }
  }

  public List<AbstractAnalysis> idSearch(String studyId, IdSearchRequest request){
    val session = em.unwrap(Session.class);
    val q = session.getNamedNativeQuery(IdView.ID_SEARCH_QUERY_NAME);
    q.setParameter(ModelAttributeNames.STUDY_ID, studyId);
    q.setParameter(ModelAttributeNames.DONOR_ID, request.getDonorId());
    q.setParameter(ModelAttributeNames.SPECIMEN_ID, request.getSpecimenId());
    q.setParameter(ModelAttributeNames.SAMPLE_ID, request.getSampleId());
    q.setParameter(ModelAttributeNames.OBJECT_ID, request.getObjectId());

    val analyses = ImmutableList.<AbstractAnalysis>builder();
    for (val result : q.getResultList()){
      val idViewProjection = (IdViewProjection)result;
      analyses.add(convertToAnalysis(idViewProjection));
    }
    return analyses.build();
  }

  public Set<Analysis> idSearch_Good(String studyId, IdSearchRequest request){
    val session = em.unwrap(Session.class);
    val q = session.getNamedNativeQuery("idSearch3");
    q.setParameter("studyId", studyId);
    q.setParameter("donorId", request.getDonorId());
    q.setParameter("specimenId", request.getSpecimenId());
    q.setParameter("sampleId", request.getSampleId());
    q.setParameter("objectId", request.getObjectId());

    val analyses = ImmutableSet.<Analysis>builder();
    for (val result : q.getResultList()){
      val idView = (IdView)result;
      analyses.add(
          createAnalysis(
              idView.getStudyId(),
              idView.getAnalysisId(),
              idView.getAnalysisState(),
              idView.getAnalysisType())
      );
    }
    return analyses.build();
  }

  private static Analysis convertToAnalysis(IdViewProjection proj){
    return createAnalysis(proj.getStudyId(), proj.getAnalysisId(),
        proj.getAnalysisState(), proj.getAnalysisType());
  }

  private static Analysis createAnalysis(String studyId, String id, String state, String type){
    val a = new Analysis();
    a.setStudy(studyId);
    a.setAnalysisType(type);
    a.setAnalysisId(id);
    a.setAnalysisState(state);
    return a;
  }

  @SneakyThrows
  private static InfoSearchResponse mapWithInfo(Object[] results){
    return createWithInfo(extractAnalysisId(results), readTree(extractInfo(results)));
  }

  private static String extractAnalysisId(Object[] result){
    return (String)result[0];
  }

  private static String extractInfo(Object[] result){
    return (String)result[1];
  }

}
