package bio.overture.song.server.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class AnalysisType {
  @NonNull private final UUID id;
  @NonNull private final String name;
  @NonNull private final Integer version;
  @NonNull private final JsonNode schema;
}
