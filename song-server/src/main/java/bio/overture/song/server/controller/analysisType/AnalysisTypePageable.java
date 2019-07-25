package bio.overture.song.server.controller.analysisType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.Value;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.util.StringUtils.isEmpty;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ID;
import static bio.overture.song.server.model.enums.ModelAttributeNames.LIMIT;
import static bio.overture.song.server.model.enums.ModelAttributeNames.NAME;
import static bio.overture.song.server.model.enums.ModelAttributeNames.OFFSET;
import static bio.overture.song.server.model.enums.ModelAttributeNames.VERSION;

@Value
@Builder
public class AnalysisTypePageable implements Pageable {

  /**
   * Constants
   */
  private final int DEFAULT_LIMIT = 20;
  private final int DEFAULT_PAGE_NUM = 0;

  /**
   * Config
   */
  private final String limit;
  private final String offset;
  private final String sortOrder;
  private final String sort;

  @Override
  public int getPageNumber() {
    return 0;
  }

  @Override
  public int getPageSize() {
    return resolveInteger(limit, LIMIT, DEFAULT_LIMIT);
  }

  @Override
  public long getOffset() {
    return resolveInteger(offset, OFFSET, DEFAULT_PAGE_NUM);
  }

  private static Integer resolveInteger(String stringValue, String paramName, int defaultValue){
    if (isEmpty(stringValue)) {
      return defaultValue;
    } else {
      try {
        return parseInt(stringValue);
      } catch (NumberFormatException e){
        throw buildServerException(AnalysisTypePageable.class, MALFORMED_PARAMETER,
            "The %s value '%s' is not an integer", paramName, stringValue);
      }
    }
  }

  private static final Set<String> ALLOWED_DIRECTION_VARIABLES = Arrays.stream(Direction.values())
      .map(Enum::name)
      .map(x -> ImmutableList.of(x.toUpperCase(), x.toLowerCase()))
      .flatMap(Collection::stream)
      .collect(toImmutableSet());

  private static Direction resolveSortOrder(String sortOrder){
    // set default sort direction
    Direction direction = DESC;
    if (!isEmpty(sortOrder)){
      if (ASC.name().equals(sortOrder.toUpperCase()) ){
        direction = ASC;
      } else if (!DESC.name().equals(sortOrder.toUpperCase())){
        throw buildServerException(AnalysisTypePageable.class, MALFORMED_PARAMETER,
            "The sortOrder value '%s' is not one of [%s]", sortOrder, COMMA.join(ALLOWED_DIRECTION_VARIABLES));
      }
    }
    return direction;
  }

  @Override
  public Sort getSort() {
    val direction  = resolveSortOrder(sortOrder);
    val sortVariables = resolveSortVariables(sort);
    return new Sort(direction, sortVariables);
  }

  @Override
  public Pageable next() {
    return null;
  }

  @Override
  public Pageable previousOrFirst() {
    return null;
  }

  @Override
  public Pageable first() {
    return null;
  }

  @Override
  public boolean hasPrevious() {
    return false;
  }

  private static final Set<String> ALLOWED_SORT_VARIABLES = ImmutableSet.of(VERSION, NAME);

  private static List<String> resolveSortVariables(String sort){
    // Default sortVariable is ID
    if (isEmpty(sort)){
      return ImmutableList.of(ID);
    } else {
      return stream(sort.split(","))
          .map(x -> {
            // Sorting by ID is equivalent to sorting by version
            if (x.equals(VERSION)){
              return ID;
            } else {
              checkServer(ALLOWED_SORT_VARIABLES.contains(x),
                  AnalysisTypePageable.class, MALFORMED_PARAMETER,
                  "The sort variable '%s' is not one or more of [%s]",
                  x, COMMA.join(ALLOWED_SORT_VARIABLES));
              return x;
            }
          })
          .collect(toImmutableList());
    }
  }

}
