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

  public Optional<String> getName() {
    return Optional.ofNullable(name);
  }

  public Optional<Integer> getVersion() {
    return Optional.ofNullable(version);
  }
}
