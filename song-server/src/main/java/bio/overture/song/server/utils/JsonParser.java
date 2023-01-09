package bio.overture.song.server.utils;

import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_TYPE;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class JsonParser {

  public static Optional<JsonNode> extractNode(JsonNode root, String field) {
    if (root.hasNonNull(field)) {
      return Optional.ofNullable(root.path(field));
    }
    return Optional.empty();
  }

  public static Optional<JsonNode> extractAnalysisTypeFromPayload(JsonNode payload) {
    return extractNode(payload, ANALYSIS_TYPE);
  }
}
