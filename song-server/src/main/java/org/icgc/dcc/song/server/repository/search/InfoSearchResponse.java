package org.icgc.dcc.song.server.repository.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.Value;

import static java.util.Objects.isNull;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoSearchResponse {

  @NonNull private final String analysisId;
  private final JsonNode info;

  @JsonIgnore
  public boolean hasInfo(){
    return !isNull(info);
  }

  public static InfoSearchResponse createWithoutInfo(String analysisId) {
    return createWithInfo(analysisId, null);
  }

  public static InfoSearchResponse createWithInfo(String analysisId, JsonNode info) {
    return new InfoSearchResponse(analysisId, info);
  }


}
