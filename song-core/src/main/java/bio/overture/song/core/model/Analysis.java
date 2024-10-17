package bio.overture.song.core.model;

import bio.overture.song.core.model.enums.AnalysisStates;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Analysis extends DynamicData {

  private String analysisId;
  private String studyId;
  private AnalysisStates analysisState;
  private AnalysisTypeId analysisType;
  private List<FileDTO> files;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private LocalDateTime firstPublishedAt;
  private LocalDateTime publishedAt;

  private SortedSet<AnalysisStateChange> analysisStateHistory;
}
