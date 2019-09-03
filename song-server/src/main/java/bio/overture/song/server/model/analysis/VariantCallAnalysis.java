package bio.overture.song.server.model.analysis;

import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;

import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.experiment.VariantCall;
import com.fasterxml.jackson.annotation.JsonGetter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@Deprecated
@AllArgsConstructor
@RequiredArgsConstructor
public class VariantCallAnalysis {

  private String analysisId;
  private String study;
  private String analysisState = UNPUBLISHED.name();
  private List<CompositeEntity> sample;
  private List<FileEntity> file;
  private VariantCall experiment;

  @JsonGetter
  public String getAnalysisTypeId() {
    return "variantCall:1";
  }
}
