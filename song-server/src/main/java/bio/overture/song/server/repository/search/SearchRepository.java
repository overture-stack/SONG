/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

package bio.overture.song.server.repository.search;

import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.entity.IdView;
import bio.overture.song.server.model.enums.ModelAttributeNames;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.Session;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class SearchRepository {

  private final EntityManager em;

  public List<Analysis> idSearch(String studyId, IdSearchRequest request) {
    val session = em.unwrap(Session.class);
    val q = session.getNamedNativeQuery(IdView.ID_SEARCH_QUERY_NAME);
    q.setParameter(ModelAttributeNames.STUDY_ID, studyId);
    q.setParameter(ModelAttributeNames.OBJECT_ID, request.getObjectId());

    val analyses = ImmutableList.<Analysis>builder();
    for (val result : q.getResultList()) {
      val idViewProjection = (IdView.IdViewProjection) result;
      analyses.add(convertToAnalysis(idViewProjection));
    }
    return analyses.build();
  }

  private static Analysis convertToAnalysis(IdView.IdViewProjection proj) {
    return createAnalysis(proj.getStudyId(), proj.getAnalysisId(), proj.getAnalysisState());
  }

  private static Analysis createAnalysis(String studyId, String id, String state) {
    val a = new Analysis();
    a.setStudyId(studyId);
    a.setAnalysisId(id);
    a.setAnalysisState(state);
    return a;
  }
}
