package bio.overture.song.server.repository.specification;

import static bio.overture.song.server.model.enums.ModelAttributeNames.NAME;
import static bio.overture.song.server.model.enums.ModelAttributeNames.VERSION;
import static bio.overture.song.server.utils.CollectionUtils.isCollectionBlank;

import bio.overture.song.server.model.entity.AnalysisSchema;
import java.util.Collection;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

public class AnalysisSchemaSpecification {

  /**
   * Dynamically generate a query depending on supplied method parameters. If there are names or
   * versions defined, then the query will filter on a combination of those. If none are defined,
   * then a findAll query results.
   */
  public static Specification<AnalysisSchema> buildListQuery(
      @Nullable Collection<String> names, @Nullable Collection<Integer> versions) {
    return (root, query, builder) -> {
      val definedNames = !isCollectionBlank(names);
      val definedVersions = !isCollectionBlank(versions);

      if (definedNames && definedVersions) {
        return builder.and(
            whereInIdsPredicate(root, NAME, names), whereInIdsPredicate(root, VERSION, versions));
      } else if (definedNames) {
        return whereInIdsPredicate(root, NAME, names);
      } else if (definedVersions) {
        return whereInIdsPredicate(root, VERSION, versions);
      } else {
        return null;
      }
    };
  }

  private static Predicate whereInIdsPredicate(
      Root<AnalysisSchema> root, String columnName, Collection values) {
    return root.get(columnName).in(values);
  }
}
