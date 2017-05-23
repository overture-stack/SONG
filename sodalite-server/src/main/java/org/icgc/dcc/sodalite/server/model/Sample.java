package org.icgc.dcc.sodalite.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.val;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "studyId", "specimenId", "sampleId", "sampleSubmitterId", "sampleType", "files"
})
public class Sample extends AbstractEntity {

  @JsonProperty("studyId")
  private String studyId;

  @JsonProperty("specimenId")
  private String specimenId;

  @JsonProperty("sampleId")
  private String sampleId;

  @JsonProperty("sampleSubmitterId")
  private String sampleSubmitterId;

  @JsonProperty("sampleType")
  private SampleType sampleType;

  @JsonProperty("files")
  private Collection<File> files = new ArrayList<File>();

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

  public Sample withStudyId(String studyId) {
    this.studyId = studyId;
    return this;
  }

  @JsonProperty("specimenId")
  public String getSpecimenId() {
    return specimenId;
  }

  @JsonProperty("specimenId")
  public void setSpecimenId(String specimenId) {
    this.specimenId = specimenId;
  }

  public Sample withSpecimenId(String specimenId) {
    this.specimenId = specimenId;
    return this;
  }

  @JsonProperty("sampleId")
  public String getSampleId() {
    return sampleId;
  }

  @JsonProperty("sampleId")
  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  public Sample withSampleId(String sampleId) {
    this.sampleId = sampleId;
    return this;
  }

  @JsonProperty("sampleSubmitterId")
  public String getSampleSubmitterId() {
    return sampleSubmitterId;
  }

  @JsonProperty("sampleSubmitterId")
  public void setSampleSubmitterId(String sampleSubmitterId) {
    this.sampleSubmitterId = sampleSubmitterId;
  }

  public Sample withSampleSubmitterId(String sampleSubmitterId) {
    this.sampleSubmitterId = sampleSubmitterId;
    return this;
  }

  @JsonProperty("sampleType")
  public SampleType getSampleType() {
    return sampleType;
  }

  @JsonProperty("sampleType")
  public void setSampleType(SampleType sampleType) {
    this.sampleType = sampleType;
  }

  public Sample withSampleType(SampleType sampleType) {
    this.sampleType = sampleType;
    return this;
  }

  @JsonProperty("files")
  public Collection<File> getFiles() {
    return files;
  }

  @JsonProperty("files")
  public void setFiles(Collection<File> files) {
    if (files != null) {
      this.files = files;
    }
  }

  public Sample withFiles(Collection<File> files) {
    if (files != null) {
      this.files = files;
    }
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

  public Sample withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  public void addFile(File f) {
    files.add(f);
  }

  @Override
  public void propagateKeys() {
    for (val f : files) {
      f.setStudyId(studyId);
      f.setSampleId(sampleId);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((files == null) ? 0 : files.hashCode());
    result = prime * result + ((sampleId == null) ? 0 : sampleId.hashCode());
    result = prime * result + ((sampleSubmitterId == null) ? 0 : sampleSubmitterId.hashCode());
    result = prime * result + ((sampleType == null) ? 0 : sampleType.hashCode());
    result = prime * result + ((specimenId == null) ? 0 : specimenId.hashCode());
    result = prime * result + ((studyId == null) ? 0 : studyId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    Sample other = (Sample) obj;
    if (files == null) {
      if (other.files != null) return false;
    } else if (!files.equals(other.files)) return false;
    if (sampleId == null) {
      if (other.sampleId != null) return false;
    } else if (!sampleId.equals(other.sampleId)) return false;
    if (sampleSubmitterId == null) {
      if (other.sampleSubmitterId != null) return false;
    } else if (!sampleSubmitterId.equals(other.sampleSubmitterId)) return false;
    if (sampleType != other.sampleType) return false;
    if (specimenId == null) {
      if (other.specimenId != null) return false;
    } else if (!specimenId.equals(other.specimenId)) return false;
    if (studyId == null) {
      if (other.studyId != null) return false;
    } else if (!studyId.equalsIgnoreCase(other.studyId)) return false;
    return true;
  }

}
