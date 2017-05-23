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
import com.google.common.base.Strings;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "uploadId", "studyId", "state", "createdAt", "updatedAt", "errors", "payload" })

public class SubmissionStatus {

  @JsonProperty("uploadId")
  private String uploadId;

  @JsonProperty("studyId")
  private String studyId;

  @JsonProperty("state")
  private AnalysisState state;

  @JsonProperty("errors")
  private List<String> errors;

  @JsonProperty("payload")
  private String payload;

  @JsonProperty("analysisObject")
  private String analysisObject;
  
  @JsonProperty("createdAt")
  private LocalDateTime createdAt;

  @JsonProperty("createdBy")
  private String createdBy;
  
  @JsonProperty("updatedAt")
  private LocalDateTime updatedAt;

  @JsonProperty("updatedBy")
  private String updatedBy;
  
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("uploadId")
  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  public SubmissionStatus withUploadId(String uploadId) {
    this.uploadId = uploadId;
    return this;
  }

  @JsonProperty("studyId")
  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public SubmissionStatus withStudyId(String studyId) {
    this.studyId = studyId;
    return this;
  }

  @JsonProperty("state")
  public AnalysisState getState() {
    return state;
  }

  @JsonProperty("state")
  public void setState(AnalysisState state) {
    this.state = state;
  }

  public SubmissionStatus withState(AnalysisState state) {
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

  public SubmissionStatus withPayload(String payload) {
    this.payload = payload;
    return this;
  }

  @JsonRawValue
  public String getAnalysisObject() {
    return analysisObject;
  }

  @JsonProperty("analysisObject")
  public void setAnalysisObject(String analysisObject) {
    this.analysisObject = analysisObject;
  }

  public SubmissionStatus withAnalysisObject(String analysisObject) {
    this.analysisObject = analysisObject;
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

  public SubmissionStatus withCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }

  @JsonProperty("createdBy")
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public SubmissionStatus withCreatedBy(String createdBy) {
    this.createdBy = createdBy;
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

  public SubmissionStatus withUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  @JsonProperty("updatedBy")
  public String getUpdatedBy() {
    return updatedBy;
  }

  @JsonProperty("updatedBy")
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public SubmissionStatus withUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
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

  public SubmissionStatus withError(String error) {
    if (this.errors == null) {
      this.errors = new ArrayList<String>();
    }
    this.errors.add(error);
    return this;
  }
  
  public boolean isMissing() {
    return Strings.isNullOrEmpty(this.uploadId);
  }
}
