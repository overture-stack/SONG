package bio.overture.song.server.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_TYPE_ID;

@NoArgsConstructor(access = PRIVATE)
public class JsonParser {

  public static Optional<JsonNode> extractNode(JsonNode root, String field) {
    if (root.has(field)) {
      return Optional.ofNullable(root.path(field));
    }
    return Optional.empty();
  }

  public static Optional<String> extractAnalysisTypeIdFromPayload(JsonNode payload) {
    return extractNode(payload, ANALYSIS_TYPE_ID).map(JsonNode::asText);
  }
}
