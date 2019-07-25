package bio.overture.song.server.controller.analysisType;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Value
@Builder
public class AnalysisTypePageable implements Pageable {

  /**
   * Config
   */
  private final int limit;
  private final int offset;
  @NonNull private final Sort sort;

  @Override
  public int getPageNumber() {
    return 0;
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  @Override
  public Sort getSort() {
    return sort;
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
