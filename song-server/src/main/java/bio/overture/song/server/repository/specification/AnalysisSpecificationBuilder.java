package bio.overture.song.server.repository.specification;

import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_DATA;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_SCHEMA;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY;
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
public class AnalysisSpecificationBuilder extends AbstractSpecificationBuilder<Analysis, String> {

  private final boolean fetchAnalysisSchema;
  private final boolean fetchAnalysisData;

  @Override
  protected Root<Analysis> setupFetchStrategy(Root<Analysis> root) {
    if (fetchAnalysisSchema) {
      root.fetch(ANALYSIS_SCHEMA, LEFT);
    }
    if (fetchAnalysisData) {
      root.fetch(ANALYSIS_DATA, LEFT);
    }
    return root;
  }

  public Specification<Analysis> buildByStudyAndAnalysisStates(
      @NonNull String study, @NonNull Collection<String> analysisStates) {
    return (fromUser, query, builder) -> {
      val root = setupFetchStrategy(fromUser);
      return builder.and(
          equalsStudyPredicate(root, builder, study), whereStatesInPredicate(root, analysisStates));
    };
  }

  @Override
  public Specification<Analysis> buildById(@NonNull String analysisId) {
    return (fromUser, query, builder) -> {
      val root = setupFetchStrategy(fromUser);
      return equalsAnalysisIdPredicate(root, builder, analysisId);
    };
  }

  @Override
  public Specification<Analysis> buildByIds(@NonNull Collection<String> analysisIds) {
    return (fromUser, query, builder) -> {
      val root = setupFetchStrategy(fromUser);
      return whereInAnalysisIdsPredicate(root, analysisIds);
    };
  }

  private Predicate whereInAnalysisIdsPredicate(Root<Analysis> root, Collection<String> ids) {
    return root.get(ModelAttributeNames.ANALYSIS_ID).in(ids);
  }

  private Predicate equalsAnalysisIdPredicate(
      Root<Analysis> root, CriteriaBuilder builder, String analysisId) {
    return builder.equal(root.get(ModelAttributeNames.ANALYSIS_ID), analysisId);
  }

  private Predicate whereStatesInPredicate(Root<Analysis> root, Collection<String> analysisStates) {
    return root.get(ModelAttributeNames.ANALYSIS_STATE).in(analysisStates);
  }

  private Predicate equalsStudyPredicate(
      Root<Analysis> root, CriteriaBuilder builder, String study) {
    return builder.equal(root.get(STUDY), study);
  }
}
