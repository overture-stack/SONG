package org.icgc.dcc.sodalite.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "sequencingRead", "study" })
public class SequencingReadDocument {

  @JsonProperty("sequencingRead")
  private SequencingRead sequencingRead;
  @JsonProperty("study")
  private Study study;

  @JsonProperty("sequencingRead")
  public SequencingRead getSequencingRead() {
    return sequencingRead;
  }

  @JsonProperty("sequencingRead")
  public void setSequencingRead(SequencingRead sequencingRead) {
    this.sequencingRead = sequencingRead;
  }

  public SequencingReadDocument withSequencingRead(SequencingRead sequencingRead) {
    this.sequencingRead = sequencingRead;
    return this;
  }

  @JsonProperty("study")
  public Study getStudy() {
    return study;
  }

  @JsonProperty("study")
  public void setStudy(Study study) {
    this.study = study;
  }

  public SequencingReadDocument withStudy(Study study) {
    this.study = study;
    return this;
  }
  
}
