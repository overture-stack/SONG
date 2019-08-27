package bio.overture.song.server.model.dto;

import static bio.overture.song.core.utils.JsonUtils.convertValue;

import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Payload {

  @NonNull private final Map<String, Object> data = new TreeMap<>();

  private String study;
  private String analysisId;
  private String analysisTypeId;
  private List<CompositeEntity> sample;
  private List<FileEntity> file;

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
