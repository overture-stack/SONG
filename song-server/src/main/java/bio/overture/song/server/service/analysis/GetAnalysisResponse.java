package bio.overture.song.server.service.analysis;

import bio.overture.song.server.model.analysis.Analysis;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class GetAnalysisResponse {
  @NonNull private List<Analysis> analyses;
  @NonNull private long totalAnalyses;
  @NonNull private int totalPages;
  @NonNull private int currentTotalAnalyses;
  @NonNull private boolean hasNext;
}
