package bio.overture.song.core.model;

import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTypeOptions {
  private List<String> fileTypes;
}
