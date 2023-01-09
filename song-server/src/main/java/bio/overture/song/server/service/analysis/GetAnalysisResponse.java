package bio.overture.song.server.service.analysis;

import bio.overture.song.server.model.analysis.Analysis;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class GetAnalysisResponse {
  @NonNull private List<Analysis> analyses;
  private int totalAnalyses;
  private int currentTotalAnalyses;
}
