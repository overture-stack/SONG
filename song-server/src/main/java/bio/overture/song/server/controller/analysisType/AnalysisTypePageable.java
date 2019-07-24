package bio.overture.song.server.controller.analysisType;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import static bio.overture.song.server.model.enums.ModelAttributeNames.ID;

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

  @Override
  public int getPageNumber() {
    return 0;
  }

  @Override
  public int getPageSize() {
    if (StringUtils.isEmpty(limit)) {
      return DEFAULT_LIMIT;
    } else {
      return Integer.parseInt(limit);
    }
  }

  @Override
  public long getOffset() {
    if (StringUtils.isEmpty(offset)) {
      return DEFAULT_PAGE_NUM;
    } else {
      return Integer.parseInt(offset);
    }
  }

  @Override
  public Sort getSort() {
    // set default sort direction
    Sort.Direction direction = Sort.Direction.DESC;

    if ((!StringUtils.isEmpty(sortOrder)) && "asc".equals(sortOrder.toLowerCase())) {
      direction = Sort.Direction.ASC;
    }
    // Need to sort by ORDER_ID so that the version can be calculated
    return new Sort(direction, ID);
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

}
