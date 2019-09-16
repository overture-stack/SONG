package bio.overture.song.server.model.dto.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_ABSENT)
public class RegisterAnalysisTypeRequest {
  private String name;
  private JsonNode schema;
}
