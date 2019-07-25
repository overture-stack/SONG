package bio.overture.song.server.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class AnalysisType {
  @NonNull private final String id;
  @NonNull private final String name;
  @NonNull private final Integer version;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final JsonNode schema;
}
