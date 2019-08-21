package bio.overture.song.server.utils;

import static org.junit.Assert.assertTrue;

import bio.overture.song.server.model.analysis.Analysis2;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;

public class TestAnalysis {

  public static JsonNode extractNode(Analysis2 a, String... fieldNames) {
    JsonNode node = a.getAnalysisData().getData();
    for (val fieldName : fieldNames) {
      assertTrue(node.has(fieldName));
      node = node.path(fieldName);
    }
    return node;
  }

  public static String extractString(Analysis2 a, String... fieldNames) {
    return extractNode(a, fieldNames).textValue();
  }

  public static boolean extractBoolean(Analysis2 a, String... fieldNames) {
    return extractNode(a, fieldNames).booleanValue();
  }

  public static long extractLong(Analysis2 a, String... fieldNames) {
    return extractNode(a, fieldNames).longValue();
  }
}
