package org.icgc.dcc.sodalite.server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.Data;
import lombok.val;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "uploadId", "studyId", "state", "createdAt", "updatedAt", "errors", "payload"
})

@Data
public class Upload {

  public final static String CREATED = "CREATED";
  public final static String VALIDATED = "VALIDATED";
  public final static String VALIDATION_ERROR = "VALIDATION_ERROR";
  public final static String UPLOADED = "UPLOADED";
  public final static String PUBLISHED = "PUBLISHED";

  private String uploadId = "";
  private String studyId = "";
  private String state = "";
  private List<String> errors = new ArrayList<>();
  private String payload = "";
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static Upload create(String id, String study, String state, String errors,
      String payload, LocalDateTime created, LocalDateTime updated) {
    val u = new Upload();

    u.setUploadId(id);
    u.setStudyId(study);
    u.setState(state);
    u.setErrors(errors);
    u.setPayload(payload);
    u.setCreatedAt(created);
    u.setUpdatedAt(updated);

    return u;
  }

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonRawValue
  public String getPayload() {
    return payload;
  }

  public void setErrors(String errorString) {
    if (errorString == null) {
      errorString = "";
    }

    this.errors.clear();
    this.errors.addAll(Arrays.asList(errorString.split("\\|")));
  }

  public void addErrors(Collection<String> errors) {
    errors.addAll(errors);
  }

}
