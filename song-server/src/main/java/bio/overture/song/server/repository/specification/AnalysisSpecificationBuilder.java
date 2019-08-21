package bio.overture.song.server.repository.specification;

import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_DATA;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_SCHEMA;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY;
import static javax.persistence.criteria.JoinType.LEFT;

import bio.overture.song.server.model.analysis.Analysis2;
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
public class AnalysisSpecificationBuilder extends AbstractSpecificationBuilder<Analysis2, String> {

  private final boolean fetchAnalysisSchema;
  private final boolean fetchAnalysisData;

  @Override
  protected Root<Analysis2> setupFetchStrategy(Root<Analysis2> root) {
    if (fetchAnalysisSchema) {
      root.fetch(ANALYSIS_SCHEMA, LEFT);
    }
    if (fetchAnalysisData) {
      root.fetch(ANALYSIS_DATA, LEFT);
    }
    return root;
  }

  public Specification<Analysis2> buildByStudyAndAnalysisStates(
      @NonNull String study, @NonNull Collection<String> analysisStates) {
    return (fromUser, query, builder) -> {
      val root = setupFetchStrategy(fromUser);
      return builder.and(
          equalsStudyPredicate(root, builder, study), whereStatesInPredicate(root, analysisStates));
    };
  }

  private Predicate whereStatesInPredicate(
      Root<Analysis2> root, Collection<String> analysisStates) {
    return root.get(ModelAttributeNames.STATE).in(analysisStates);
  }

  private Predicate equalsStudyPredicate(
      Root<Analysis2> root, CriteriaBuilder builder, String study) {
    return builder.equal(root.get(STUDY), study);
  }
}
