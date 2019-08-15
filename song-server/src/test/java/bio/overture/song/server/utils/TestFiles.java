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

package bio.overture.song.server.utils;

import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import bio.overture.song.server.model.Metadata;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.util.Joiners;

@Slf4j
public class TestFiles {

  public static final String SEARCH_TEST_DIR = "documents/search";
  private static final String NAME = "name";
  public static final String EMPTY_STRING = "";
  public static final String DEFAULT_EMPTY_VALUE = null;

  @SneakyThrows
  public static String getJsonStringFromClasspath(String name) {
    return toJson(getJsonNodeFromClasspath(name));
  }

  private static URL getTestResourceUrl() {
    return TestFiles.class.getClassLoader().getResource("./");
  }

  public static Path getTestResourceFilePath(String filename) {
    return Paths.get(Joiners.PATH.join(getTestResourceUrl().getPath(), filename));
  }

  @SneakyThrows
  public static JsonNode getJsonNodeFromClasspath(String pathname) {
    InputStream is1 = currentThread().getContextClassLoader().getResourceAsStream(pathname);
    checkNotNull(is1, "null reference. Path '%s' could be incorrect", pathname);
    return readTree(is1);
  }

  public static boolean isTestFileExist(String filename) {
    return getTestResourceFilePath(filename).toFile().exists();
  }

  public static String getInfoName(Metadata metadata) {
    val info = metadata.getInfo();
    assertTrue(info.has(NAME));
    return metadata.getInfo().get(NAME).textValue();
  }

  public static String getInfoValue(@NonNull Metadata metadata, @NonNull String key) {
    assertTrue(metadata.getInfo().has(key));
    return metadata.getInfo().path(key).textValue();
  }

  public static void assertInfoKVPair(
      @NonNull Metadata metadata, @NonNull String key, @NonNull Object expectedValue) {
    assertTrue(
        format("The input metadata does not have the key '%s'", key), metadata.getInfo().has(key));
    val actualValue = metadata.getInfo().path(key).textValue();
    assertEquals(
        format(
            "Failed since field '%s' has actual=%s and expected=%s",
            key, actualValue, expectedValue),
        actualValue,
        expectedValue);
  }
}
