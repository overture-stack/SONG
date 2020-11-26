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

package bio.overture.song.server.repository.specification;

import static bio.overture.song.server.model.enums.ModelAttributeNames.*;
import static javax.persistence.criteria.JoinType.LEFT;

import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.enums.ModelAttributeNames;
import java.util.Collection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class AnalysisSpecificationBuilder {

  private final boolean fetchAnalysisSchema;
  private final boolean fetchAnalysisData;
  private final boolean fetchAnalysisStateHistory;

  public Specification<Analysis> buildByStudyAndAnalysisStates(
      @NonNull String study, @NonNull Collection<String> analysisStates) {
    return (fromUser, query, builder) -> {
      val root = setupFetchStrategy(fromUser);
      query.distinct(true);
      return builder.and(
          equalsStudyPredicate(root, builder, study), whereStatesInPredicate(root, analysisStates));
    };
  }

  public Specification<Analysis> buildById(@NonNull String analysisId) {
    return (fromUser, query, builder) -> {
      val root = setupFetchStrategy(fromUser);
      return equalsAnalysisIdPredicate(root, builder, analysisId);
    };
  }

  public Specification<Analysis> buildByIds(@NonNull Collection<String> analysisIds) {
    return (fromUser, query, builder) -> {
      val root = setupFetchStrategy(fromUser);
      return whereInAnalysisIdsPredicate(root, analysisIds);
    };
  }

  private Root<Analysis> setupFetchStrategy(Root<Analysis> root) {
    if (fetchAnalysisSchema) {
      root.fetch(ANALYSIS_SCHEMA, LEFT);
    }
    if (fetchAnalysisData) {
      root.fetch(ANALYSIS_DATA, LEFT);
    }
    if (fetchAnalysisStateHistory) {
      root.fetch(ANALYSIS_STATE_HISTORY, LEFT);
    }
    return root;
  }

  private static Predicate whereInAnalysisIdsPredicate(
      Root<Analysis> root, Collection<String> ids) {
    return root.get(ModelAttributeNames.ANALYSIS_ID).in(ids);
  }

  private static Predicate equalsAnalysisIdPredicate(
      Root<Analysis> root, CriteriaBuilder builder, String analysisId) {
    return builder.equal(root.get(ModelAttributeNames.ANALYSIS_ID), analysisId);
  }

  private static Predicate whereStatesInPredicate(
      Root<Analysis> root, Collection<String> analysisStates) {
    return root.get(ModelAttributeNames.ANALYSIS_STATE).in(analysisStates);
  }

  private static Predicate equalsStudyPredicate(
      Root<Analysis> root, CriteriaBuilder builder, String study) {
    return builder.equal(root.get(STUDY_ID), study);
  }
}
