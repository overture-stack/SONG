package bio.overture.song.server.model.dto.schema;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

import bio.overture.song.core.model.AnalysisTypeOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_ABSENT)
public class RegisterAnalysisTypeRequest {
  private String name;
  private JsonNode schema;
  private AnalysisTypeOptions options;
}
