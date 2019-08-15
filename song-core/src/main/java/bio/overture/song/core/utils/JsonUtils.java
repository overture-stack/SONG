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

import static com.google.common.base.Strings.emptyToNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.val;

/** Utility functions related to deal with JSON */
public class JsonUtils {

  private static final String SINGLE_QUOTE = "'";
  private static final String DOUBLE_QUOTE = "\"";

  protected static final ObjectMapper mapper = mapper();

  public static ObjectMapper mapper() {
    val specialModule = new SimpleModule();
    specialModule.addDeserializer(String.class, SpecialStringJsonDeserializer.instance);

    val mapper =
        new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(specialModule)
            .registerModule(new JavaTimeModule());

    mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
    mapper.disable(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

    // Doesn't work! Fields with the value '""' (empty string) are not being deserialized as null.
    // mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    return mapper;
  }

  public static JsonNode readTree(String json) throws IOException {
    return mapper.readTree(json);
  }

  public static JsonNode readTree(InputStream in) throws IOException {
    return mapper.readTree(in);
  }

  public static ObjectNode ObjectNode() {
    return mapper.createObjectNode();
  }

  @SneakyThrows
  public static JsonNode read(URL url) {
    return mapper.readTree(url);
  }

  @SneakyThrows
  public static String nodeToJSON(ObjectNode node) {
    return mapper.writeValueAsString(node);
  }

  @SneakyThrows
  public static String toJson(Object o) {
    return mapper.writeValueAsString(o);
  }

  @SneakyThrows
  public static JsonNode toJsonNode(Map<String, Object> map) {
    return mapper.convertValue(map, JsonNode.class);
  }

  public static class SongPrettyPrinter extends DefaultPrettyPrinter {
    public static final SongPrettyPrinter instance = new SongPrettyPrinter(4);

    public SongPrettyPrinter(int indentSize) {
      val sb = new StringBuilder();
      for (int i = 0; i < indentSize; i++) {
        sb.append(' ');
      }
      indentArraysWith(new DefaultIndenter(sb.toString(), DefaultIndenter.SYS_LF));
    }
  }

  @SneakyThrows
  public static String toPrettyJson(Object o) {
    return mapper.writer(SongPrettyPrinter.instance).writeValueAsString(o);
  }

  @SneakyThrows
  public static <T> T fromJson(String json, Class<T> toValue) {
    return fromJson(mapper.readTree(json), toValue);
  }

  @SneakyThrows
  public static <T> T fromJson(JsonNode json, Class<T> toValue) {
    return mapper.convertValue(json, toValue);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> toMap(String json)
      throws IllegalArgumentException, IOException {
    return fromJson(json, Map.class);
  }

  public static String fromSingleQuoted(String singleQuotedJson) {
    return singleQuotedJson.replaceAll(SINGLE_QUOTE, DOUBLE_QUOTE);
  }

  public static <T> T convertValue(Object fromValue, Class<T> toValue) {
    return mapper().convertValue(fromValue, toValue);
  }

  /**
   * Since the ACCEPT_EMPTY_STRING_AS_NULL_OBJECT DeserializationFeature is not working properly,
   * created custom string deserialization handling of empty string.
   */
  public static class SpecialStringJsonDeserializer extends StdDeserializer<String> {
    public static final SpecialStringJsonDeserializer instance =
        new SpecialStringJsonDeserializer();

    public SpecialStringJsonDeserializer() {
      super(String.class);
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
      val result = StringDeserializer.instance.deserialize(jsonParser, deserializationContext);
      return emptyToNull(result);
    }
  }
}
