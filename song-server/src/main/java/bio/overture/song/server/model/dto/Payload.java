package bio.overture.song.server.model.dto;

import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.DynamicData;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Payload extends DynamicData {
  private static final ObjectMapper MAPPER = payloadObjectMapper();

  private String studyId;
  private AnalysisTypeId analysisType;
  private List<CompositeEntity> samples;
  private List<FileEntity> files;

  @SneakyThrows
  public static Payload parse(JsonNode jsonNode) {
    return parse(jsonNode.toString());
  }

  @SneakyThrows
  public static Payload parse(String jsonStr) {
    // convert to hashMap
    val jsonNode = MAPPER.readValue(jsonStr, HashMap.class);
    // writeValueAsString will remove null values from map, including nested
    // because MAPPER is configured to include NON_NULL
    val sanitized = MAPPER.writeValueAsString(jsonNode);
    // return Payload
    return MAPPER.readValue(sanitized, Payload.class);
  }

  private static ObjectMapper payloadObjectMapper() {
    val mapper = JsonUtils.mapper();
    // Hibernate does not persist values that are null inside an entity to the db.
    // Submitted payloads however can have values that are null, which can lead
    // to inconsistencies if they are being used in memory vs the data saved to db.
    // So when parsing payloads with MAPPER ensure only non_null fields are kept.
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper;
  }
}
