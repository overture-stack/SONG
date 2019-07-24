package bio.overture.song.server.model.dto.schema;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class RegisterAnalysisTypeRequest {
  private String name;
  private JsonNode schema;
}
