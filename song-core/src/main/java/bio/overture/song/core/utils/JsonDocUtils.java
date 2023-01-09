/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
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
package bio.overture.song.core.utils;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.InputStream;
import lombok.SneakyThrows;
import lombok.val;

public class JsonDocUtils {

  private static ObjectMapper buildObjectMapper() {
    return new ObjectMapper();
  }

  public static InputStream getInputStreamClasspath(String fileName) {
    val is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    checkState(!isNull(is), "The file '%s' was not found", fileName);
    return is;
  }

  @SneakyThrows
  public static JsonNode getJsonNodeFromClasspath(String fileName) {
    val mapper = buildObjectMapper();
    val is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    checkState(!isNull(is), "The file '%s' was not found", fileName);
    return mapper.readTree(is);
  }

  public static String getValue(JsonNode node, String key) {
    val result = node.get(key).textValue();
    if (Strings.isNullOrEmpty(result)) {
      throw new IllegalArgumentException("No value found for " + key);
    } else {
      return result;
    }
  }
}
