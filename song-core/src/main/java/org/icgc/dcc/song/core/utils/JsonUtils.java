/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.core.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.util.Map;

/**
 * Utility functions related to deal with JSON
 */
public class JsonUtils {

  private static final String SINGLE_QUOTE = "'";
  private static final String DOUBLE_QUOTE = "\"";

  protected static final ObjectMapper mapper = mapper();

  public static ObjectMapper mapper() {
    val mapper = new ObjectMapper().registerModule(new ParameterNamesModule())
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule());
    mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
    mapper.disable(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    return mapper;
  }

  public static JsonNode readTree(String json) throws  IOException {
    return mapper.readTree(json);
  }

  public static ObjectNode ObjectNode() {
    return mapper.createObjectNode();
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
  public static String toPrettyJson(Object o) {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @SneakyThrows
  public static <T> T fromJson(String json, Class<T> toValue) {
    return mapper.convertValue(mapper.readTree(json), toValue);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> toMap(String json)
      throws IllegalArgumentException, IOException {
    return mapper.convertValue(mapper.readTree(json), Map.class);
  }

  public static String fromSingleQuoted(String singleQuotedJson) {
    return singleQuotedJson.replaceAll(SINGLE_QUOTE, DOUBLE_QUOTE);
  }

  public static <T> T convertValue(Object fromValue, Class<T> toValue) {
    return mapper().convertValue(fromValue, toValue);
  }

}
