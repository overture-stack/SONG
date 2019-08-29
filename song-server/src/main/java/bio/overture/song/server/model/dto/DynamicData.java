package bio.overture.song.server.model.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;
import java.util.TreeMap;

import static bio.overture.song.core.utils.JsonUtils.convertValue;

@ToString
@EqualsAndHashCode
public class DynamicData {

  @NonNull private final Map<String, Object> data = new TreeMap<>();

  @JsonAnySetter
  public void addData(String key, Object value) {
    data.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public void addData(JsonNode json) {
    Map<String, Object> map = convertValue(json, Map.class);
    data.putAll(map);
  }

  @JsonAnyGetter
  public Map<String, Object> getData() {
    return data;
  }
}
