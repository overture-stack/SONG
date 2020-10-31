package bio.overture.song.core.model;

import bio.overture.song.core.model.enums.AnalysisStates;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.google.common.collect.Sets.newHashSet;

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
  private List<CompositeSample> samples;
  private List<FileDTO> files;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private LocalDateTime firstPublishedAt;
  private LocalDateTime publishedAt;

  private Set<AnalysisStateChange> analysisStateHistory;
}
