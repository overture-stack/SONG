package bio.overture.song.server.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class GetAnalysisTypeResponse {
  @NonNull private final String name;
  @NonNull private final Integer version;
  @NonNull private final JsonNode schema;
}
