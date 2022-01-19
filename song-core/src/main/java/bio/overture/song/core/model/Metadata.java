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
package bio.overture.song.core.model;

import static bio.overture.song.core.utils.JsonUtils.convertValue;
import static bio.overture.song.core.utils.JsonUtils.toMap;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;

import bio.overture.song.core.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import lombok.*;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Metadata {

  private final Map<String, Object> info = new TreeMap<>();

  @JsonAnySetter
  public void setInfo(String key, Object value) {
    info.put(key, value);
  }

  @JsonSetter
  public void setInfo(JsonNode info) {
    setInfo(JsonUtils.toJson(info));
  }

  public void setInfo(String info) {
    addInfo(info);
  }

  public void setInfo(Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      info.put(entry.getKey(), entry.getValue());
    }
  }

  @JsonGetter
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public JsonNode getInfo() {
    return JsonUtils.toJsonNode(info);
  }

  @JsonIgnore
  public String getInfoAsString() {
    return JsonUtils.toJson(info);
  }

  @SuppressWarnings("unchecked")
  public void addInfo(String json) {
    if (isNullOrEmpty(json)) {
      return;
    }
    Map<String, Object> m;
    try {
      m = toMap(json);
    } catch (IllegalArgumentException | IOException e) {
      val j = JsonUtils.ObjectNode().put("info", json);
      m = convertValue(j, Map.class);
    }
    if (!isNull(m)) {
      info.putAll(m);
    }
  }
}
