package bio.overture.song.server.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_TYPE;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_TYPE_ID;
import static bio.overture.song.server.model.enums.ModelAttributeNames.NAME;

@NoArgsConstructor(access = PRIVATE)
public class JsonParser {

  public static Optional<JsonNode> extractNode(JsonNode root, String field){
    if (root.has(field)) {
      return Optional.ofNullable(root.path(field));
    }
    return Optional.empty();
  }

  public static Optional<JsonNode> extractAnalysisType(JsonNode root){
    return extractNode(root, ANALYSIS_TYPE);
  }

  public static Optional<JsonNode> extractName(JsonNode root){
    return extractNode(root, NAME);
  }


  public static Optional<String> extractAnalysisTypeNameFromAnalysis(JsonNode analysis){
    return extractAnalysisType(analysis)
        .map(JsonParser::extractName)
        .get()
        .map(JsonNode::asText);
  }

  public static Optional<String> extractAnalysisTypeIdFromAnalysis(JsonNode analysis){
    return extractNode(analysis, ANALYSIS_TYPE_ID)
        .map(JsonNode::asText);
  }



}
