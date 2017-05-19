package org.icgc.dcc.sodalite.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.icgc.dcc.sodalite.server.model.utils.Views;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "studyId", "donorId", "specimenId", "specimenSubmitterId", "specimenClass", "specimenType", "samples" })
public class Specimen extends AbstractEntity {

  @JsonProperty("studyId")
  private String studyId;
  
  @JsonProperty("donorId")
  private String donorId;
  
  @JsonProperty("specimenId")
  private String specimenId;

  @JsonProperty("specimenSubmitterId")
  private String specimenSubmitterId;

  @JsonProperty("specimenClass")
  private SpecimenClass specimenClass;

  @JsonProperty("specimenType")
  private SpecimenType specimenType;

  @JsonView(Views.Document.class)
  @JsonProperty("sample")
  private Sample sample;
  
  @JsonView(Views.Collection.class)
  @JsonProperty("samples")
  private Collection<Sample> samples = new ArrayList<Sample>();

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

  public Specimen withStudyId(String studyId) {
    this.studyId = studyId;
    return this;
  }

  @JsonProperty("donorId")
  public String getDonorId() {
    return donorId;
  }

  @JsonProperty("donorId")
  public void setDonorId(String donorId) {
    this.donorId = donorId;
  }

  public Specimen withDonorId(String donorId) {
    this.donorId = donorId;
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

  public Specimen withSpecimenId(String specimenId) {
    this.specimenId = specimenId;
    return this;
  }

  @JsonProperty("specimenSubmitterId")
  public String getSpecimenSubmitterId() {
    return specimenSubmitterId;
  }

  @JsonProperty("specimenSubmitterId")
  public void setSpecimenSubmitterId(String specimenSubmitterId) {
    this.specimenSubmitterId = specimenSubmitterId;
  }

  public Specimen withSpecimenSubmitterId(String specimenSubmitterId) {
    this.specimenSubmitterId = specimenSubmitterId;
    return this;
  }

  @JsonProperty("specimenClass")
  public SpecimenClass getSpecimenClass() {
    return specimenClass;
  }

  @JsonProperty("specimenClass")
  public void setSpecimenClass(SpecimenClass specimenClass) {
    this.specimenClass = specimenClass;
  }

  public Specimen withSpecimenClass(SpecimenClass specimenClass) {
    this.specimenClass = specimenClass;
    return this;
  }

  @JsonProperty("specimenType")
  public SpecimenType getSpecimenType() {
    return specimenType;
  }

  @JsonProperty("specimenType")
  public void setSpecimenType(SpecimenType specimenType) {
    this.specimenType = specimenType;
  }

  public Specimen withSpecimenType(SpecimenType specimenType) {
    this.specimenType = specimenType;
    return this;
  }

  @JsonView(Views.Collection.class)
  @JsonProperty("samples")
  public Collection<Sample> getSamples() {
    return samples;
  }

  @JsonProperty("samples")
  public void setSamples(Collection<Sample> samples) {
    if (samples != null) {
      this.samples = samples;
    }
  }

  public void addSample(Sample sample) {
    samples.add(sample);
  }

  public Specimen withSamples(Collection<Sample> samples) {
    if (samples != null) {
      this.samples = samples;
    }
    return this;
  }

  @JsonView(Views.Document.class)
  @JsonProperty("sample")
  public Sample getSample() {
    return sample;
  }

  @JsonProperty("sample")
  public void setSample(Sample sample) {
    this.sample = sample;
  }

  public Specimen withSample(Sample sample) {
    this.sample = sample;
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

  public Specimen withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }
  
  @Override
  public void propagateKeys() {
    if (sample != null) {
      sample.setStudyId(studyId);
      sample.setSpecimenId(specimenId);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((donorId == null) ? 0 : donorId.hashCode());
    result = prime * result + ((sample == null) ? 0 : sample.hashCode());
    result = prime * result + ((specimenClass == null) ? 0 : specimenClass.hashCode());
    result = prime * result + ((specimenId == null) ? 0 : specimenId.hashCode());
    result = prime * result + ((specimenSubmitterId == null) ? 0 : specimenSubmitterId.hashCode());
    result = prime * result + ((specimenType == null) ? 0 : specimenType.hashCode());
    result = prime * result + ((studyId == null) ? 0 : studyId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    Specimen other = (Specimen) obj;
    if (donorId == null) {
      if (other.donorId != null)
        return false;
    }
    else if (!donorId.equals(other.donorId))
      return false;
    if (sample == null) {
      if (other.sample != null)
        return false;
    }
    else if (!sample.equals(other.sample))
      return false;
    if (specimenClass != other.specimenClass)
      return false;
    if (specimenId == null) {
      if (other.specimenId != null)
        return false;
    }
    else if (!specimenId.equals(other.specimenId))
      return false;
    if (specimenSubmitterId == null) {
      if (other.specimenSubmitterId != null)
        return false;
    }
    else if (!specimenSubmitterId.equals(other.specimenSubmitterId))
      return false;
    if (specimenType != other.specimenType)
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
