/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
