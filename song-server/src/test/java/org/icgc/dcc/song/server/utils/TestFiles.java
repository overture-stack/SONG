package org.icgc.dcc.song.server.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;

import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.utils.JsonUtils.readTree;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;

public class TestFiles {

  public static final String SEARCH_TEST_DIR = "documents/search";
  private static final String NAME = "name";

  @SneakyThrows
  public static String getJsonStringFromClasspath(String name) {
    return toJson(getJsonNodeFromClasspath(name));
  }

  @SneakyThrows
  public static JsonNode getJsonNodeFromClasspath(String pathname) {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathname);
    checkNotNull(is1, "null reference. Path '%s' could be incorrect", pathname);
    return readTree(is1);
  }

  public static String getInfoName(Metadata metadata){
    val info = metadata.getInfo();
    assertThat(info.has(NAME)).isTrue();
    return metadata.getInfo().get(NAME).textValue();
  }

  public static String getInfoValue(@NonNull Metadata metadata, @NonNull String key) {
      assertThat(metadata.getInfo().has(key)).isTrue();
      return metadata.getInfo().path(key).textValue();
  }

  public static void assertInfoKVPair(@NonNull Metadata metadata, @NonNull String key, @NonNull Object expectedValue){
      assertThat(metadata.getInfo().has(key)).isTrue();
      val actualValue  = metadata.getInfo().path(key).textValue();
      assertThat(actualValue)
          .as("Failed since field '%s' has actual=%s and expected=%s", key, actualValue, expectedValue)
          .isEqualTo (expectedValue);
  }

}
