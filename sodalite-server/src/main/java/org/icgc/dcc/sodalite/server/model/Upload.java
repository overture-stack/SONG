package org.icgc.dcc.sodalite.server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "uploadId", "studyId", "state", "createdAt", "updatedAt", "errors", "payload"
})

public class Upload {

  public final static String CREATED = "CREATED";
  public final static String VALIDATED = "VALIDATED";
  public final static String VALIDATION_ERROR = "VALIDATION_ERROR";
  public final static String UPLOADED = "UPLOADED";
  public final static String PUBLISHED = "PUBLISHED";

  @JsonProperty("uploadId")
  private String uploadId;

  @JsonProperty("studyId")
  private String studyId;

  @JsonProperty("state")
  private String state;

  @JsonProperty("errors")
  private List<String> errors;

  @JsonProperty("payload")
  private String payload;

  @JsonProperty("createdAt")
  private LocalDateTime createdAt;

  @JsonProperty("updatedAt")
  private LocalDateTime updatedAt;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("uploadId")
  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  public Upload withUploadId(String uploadId) {
    this.uploadId = uploadId;
    return this;
  }

  @JsonProperty("studyId")
  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public Upload withStudyId(String studyId) {
    this.studyId = studyId;
    return this;
  }

  @JsonProperty("state")
  public String getState() {
    return state;
  }

  @JsonProperty("state")
  public void setState(String state) {
    this.state = state;
  }

  public Upload withState(String state) {
    this.state = state;
    return this;
  }

  @JsonRawValue
  public String getPayload() {
    return payload;
  }

  @JsonProperty("payload")
  public void setPayload(String payload) {
    this.payload = payload;
  }

  public Upload withPayload(String payload) {
    this.payload = payload;
    return this;
  }

  @JsonProperty("createdAt")
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Upload withCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  @JsonProperty("updatedAt")
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Upload withUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @JsonProperty("errors")
  public List<String> getErrors() {
    return (this.errors == null) ? Collections.<String> emptyList() : this.errors;
  }

  @JsonProperty("errors")
  public void setErrors(String error) {
    if (this.errors == null) {
      this.errors = new ArrayList<String>();
    }
    this.errors.add(error);
  }

  public Upload withError(String error) {
    if (this.errors == null) {
      this.errors = new ArrayList<String>();
    }
    this.errors.add(error);
    return this;
  }
}
