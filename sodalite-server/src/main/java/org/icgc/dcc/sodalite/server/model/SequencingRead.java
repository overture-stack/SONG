
package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "studyId", "analysisId", "analysisSubmitterId", "state", "aligned", "alignmentTool", "insertSize", "libraryStrategy", "pairedEnd", "referenceGenome"
})

public class SequencingRead {

  @JsonProperty("studyId")
  private String studyId;
  @JsonProperty("analysisId")
  private String analysisId;
  @JsonProperty("analysisSubmitterId")
  private String analysisSubmitterId;
  @JsonProperty("state")
  private AnalysisState state;
  @JsonProperty("aligned")
  private boolean aligned;
  @JsonProperty("alignmentTool")
  private String alignmentTool;
  @JsonProperty("insertSize")
  private int insertSize;
  @JsonProperty("libraryStrategy")
  private LibraryStrategy libraryStrategy;
  @JsonProperty("pairedEnd")
  private boolean pairedEnd;
  @JsonProperty("referenceGenome")
  private String referenceGenome;
  
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("studyId")
  public String getStudyId() {
    return studyId;
  }

  @JsonProperty("studyId")
  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public SequencingRead withStudyId(String studyId) {
    this.studyId = studyId;
    return this;
  }

  @JsonProperty("analysisId")
  public String getAnalysisId() {
    return analysisId;
  }

  @JsonProperty("analysisId")
  public void setAnalysisId(String analysisId) {
    this.analysisId = analysisId;
  }

  public SequencingRead withAnalysisId(String analysisId) {
    this.analysisId = analysisId;
    return this;
  }

  @JsonProperty("analysisSubmitterId")
  public String getAnalysisSubmitterId() {
    return analysisSubmitterId;
  }

  @JsonProperty("analysisSubmitterId")
  public void setAnalysisSubmitterId(String analysisSubmitterId) {
    this.analysisSubmitterId = analysisSubmitterId;
  }

  public SequencingRead withAnalysisSubmitterId(String analysisSubmitterId) {
    this.analysisSubmitterId = analysisSubmitterId;
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

  public SequencingRead withState(AnalysisState state) {
    this.state = state;
    return this;
  }
  
  @JsonProperty("aligned")
  public boolean isAligned() {
    return aligned;
  }

  @JsonProperty("aligned")
  public void setAligned(boolean aligned) {
    this.aligned = aligned;
  }

  public SequencingRead withAligned(boolean aligned) {
    this.aligned = aligned;
    return this;
  }

  @JsonProperty("alignmentTool")
  public String getAlignmentTool() {
    return alignmentTool;
  }

  @JsonProperty("alignmentTool")
  public void setAlignmentTool(String alignmentTool) {
    this.alignmentTool = alignmentTool;
  }

  public SequencingRead withAlignmentTool(String alignmentTool) {
    this.alignmentTool = alignmentTool;
    return this;
  }

  @JsonProperty("insertSize")
  public int getInsertSize() {
    return insertSize;
  }

  @JsonProperty("insertSize")
  public void setInsertSize(int insertSize) {
    this.insertSize = insertSize;
  }

  public SequencingRead withInsertSize(int insertSize) {
    this.insertSize = insertSize;
    return this;
  }

  @JsonProperty("libraryStrategy")
  public LibraryStrategy getLibraryStrategy() {
    return libraryStrategy;
  }

  @JsonProperty("libraryStrategy")
  public void setLibraryStrategy(LibraryStrategy libraryStrategy) {
    this.libraryStrategy = libraryStrategy;
  }

  public SequencingRead withLibraryStrategy(LibraryStrategy libraryStrategy) {
    this.libraryStrategy = libraryStrategy;
    return this;
  }

  @JsonProperty("pairedEnd")
  public boolean isPairedEnd() {
    return pairedEnd;
  }

  @JsonProperty("pairedEnd")
  public void setPairedEnd(boolean pairedEnd) {
    this.pairedEnd = pairedEnd;
  }

  public SequencingRead withPairedEnd(boolean pairedEnd) {
    this.pairedEnd = pairedEnd;
    return this;
  }

  @JsonProperty("referenceGenome")
  public String getReferenceGenome() {
    return referenceGenome;
  }

  @JsonProperty("referenceGenome")
  public void setReferenceGenome(String referenceGenome) {
    this.referenceGenome = referenceGenome;
  }

  public SequencingRead withReferenceGenome(String referenceGenome) {
    this.referenceGenome = referenceGenome;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public SequencingRead withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (aligned ? 1231 : 1237);
    result = prime * result + ((alignmentTool == null) ? 0 : alignmentTool.hashCode());
    result = prime * result + ((analysisId == null) ? 0 : analysisId.hashCode());
    result = prime * result + ((analysisSubmitterId == null) ? 0 : analysisSubmitterId.hashCode());
    result = prime * result + insertSize;
    result = prime * result + ((libraryStrategy == null) ? 0 : libraryStrategy.hashCode());
    result = prime * result + (pairedEnd ? 1231 : 1237);
    result = prime * result + ((referenceGenome == null) ? 0 : referenceGenome.hashCode());
    result = prime * result + ((state == null) ? 0 : state.hashCode());
    result = prime * result + ((studyId == null) ? 0 : studyId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SequencingRead other = (SequencingRead) obj;
    if (aligned != other.aligned)
      return false;
    if (alignmentTool == null) {
      if (other.alignmentTool != null)
        return false;
    }
    else if (!alignmentTool.equals(other.alignmentTool))
      return false;
    if (analysisId == null) {
      if (other.analysisId != null)
        return false;
    }
    else if (!analysisId.equals(other.analysisId))
      return false;
    if (analysisSubmitterId == null) {
      if (other.analysisSubmitterId != null)
        return false;
    }
    else if (!analysisSubmitterId.equalsIgnoreCase(other.analysisSubmitterId))
      return false;
    if (insertSize != other.insertSize)
      return false;
    if (libraryStrategy != other.libraryStrategy)
      return false;
    if (pairedEnd != other.pairedEnd)
      return false;
    if (referenceGenome == null) {
      if (other.referenceGenome != null)
        return false;
    }
    else if (!referenceGenome.equals(other.referenceGenome))
      return false;
    if (state == null) {
      if (other.state != null)
        return false;
    }
    else if (!state.equals(other.state))
      return false;
    if (studyId == null) {
      if (other.studyId != null)
        return false;
    }
    else if (!studyId.equalsIgnoreCase(other.studyId))
      return false;
    return true;
  }
  
  
}
