package bio.overture.song.server.model.analysis;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(NON_ABSENT)
public class AnalysisTypeId {

  private final String name;
  private final Integer version;

  public Optional<String> getName() {
    return Optional.ofNullable(name);
  }

  public Optional<Integer> getVersion() {
    return Optional.ofNullable(version);
  }
}
