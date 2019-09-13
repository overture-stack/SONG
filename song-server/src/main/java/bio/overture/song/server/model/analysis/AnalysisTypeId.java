package bio.overture.song.server.model.analysis;

import java.util.Optional;
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

  @Positive private Integer version;

  public Optional<Integer> getOptionalVersion() {
    return Optional.ofNullable(version);
  }
}
