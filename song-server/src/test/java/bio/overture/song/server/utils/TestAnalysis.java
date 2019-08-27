package bio.overture.song.server.utils;

import static org.junit.Assert.assertTrue;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.dto.Payload;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;

public class TestAnalysis {

  public static JsonNode extractPayloadNode(Payload a, String... fieldNames) {
    JsonNode node = JsonUtils.toJsonNode(a.getData());
    for (val fieldName : fieldNames) {
      assertTrue(node.has(fieldName));
      node = node.path(fieldName);
    }
    return node;
  }

  public static JsonNode extractNode(Analysis a, String... fieldNames) {
    JsonNode node = a.getAnalysisData().getData();
    for (val fieldName : fieldNames) {
      assertTrue(node.has(fieldName));
      node = node.path(fieldName);
    }
    return node;
  }

  public static String extractString(Analysis a, String... fieldNames) {
    return extractNode(a, fieldNames).textValue();
  }

  public static boolean extractBoolean(Analysis a, String... fieldNames) {
    return extractNode(a, fieldNames).booleanValue();
  }

  public static long extractLong(Analysis a, String... fieldNames) {
    return extractNode(a, fieldNames).longValue();
  }
}
