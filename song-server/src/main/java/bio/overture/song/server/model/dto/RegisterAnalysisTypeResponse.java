package bio.overture.song.server.model.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class RegisterAnalysisTypeResponse {
  @NonNull private final String name;
  private final int version;
}
