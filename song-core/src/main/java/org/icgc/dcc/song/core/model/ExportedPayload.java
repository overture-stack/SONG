package org.icgc.dcc.song.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@JsonPropertyOrder({"studyId", "payloads"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExportedPayload {
  @NonNull private String studyId;
  @NonNull private List<JsonNode> payloads;

  public static ExportedPayload createExportedPayload(String studyId, List<JsonNode> payloads) {
    return new ExportedPayload(studyId, payloads);
  }

}

