package bio.overture.song.core.model;

import bio.overture.song.core.model.enums.AnalysisStates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Analysis extends DynamicData {

  private String analysisId;
  private String study;
  private AnalysisStates analysisState;
  private AnalysisTypeId analysisType;
  private List<CompositeSample> sample;
  private List<FileDTO> file;
}
