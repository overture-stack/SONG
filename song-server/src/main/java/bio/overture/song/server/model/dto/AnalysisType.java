package bio.overture.song.server.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisType {

  @NotNull private String name;
  @NotNull private Integer version;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode schema;

  public static AnalysisType createAnalysisType(
      @NonNull String name, int version, JsonNode schema) {
    return builder().name(name).version(version).schema(schema).build();
  }
}
