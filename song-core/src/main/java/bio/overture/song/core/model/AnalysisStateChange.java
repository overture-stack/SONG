package bio.overture.song.core.model;

import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AnalysisStateChange extends DynamicData implements Comparable<AnalysisStateChange> {
  private String initialState;
  private String updatedState;
  private LocalDateTime updatedAt;

  @Override
  public int compareTo(AnalysisStateChange o) {
    return this.getUpdatedAt().compareTo(o.getUpdatedAt());
  }
}
