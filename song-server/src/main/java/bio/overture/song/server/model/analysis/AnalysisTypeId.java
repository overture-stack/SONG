package bio.overture.song.server.model.analysis;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTypeId {

  @NotNull private String name;

  @NotNull @Positive private Integer version;

  public static AnalysisTypeId createAnalysisTypeId(String name, int version) {
    return new AnalysisTypeId(name, version);
  }
}
