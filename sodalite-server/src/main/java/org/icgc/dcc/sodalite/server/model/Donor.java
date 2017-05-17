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
@JsonPropertyOrder({ "studyId", "donorId", "donorSubmitterId", "donorGender", "specimens"
})
public class Donor extends AbstractEntity {

  @JsonProperty("studyId")
  private String studyId;
  
  @JsonProperty("donorId")
  private String donorId;

  @JsonProperty("donorSubmitterId")
  private String donorSubmitterId;

  @JsonProperty("donorGender")
  private DonorGender donorGender;

  @JsonView(Views.Collection.class)
  @JsonProperty("specimens")
  private Collection<Specimen> specimens = new ArrayList<Specimen>();
  
  @JsonView(Views.Document.class)
  @JsonProperty("specimen")
  private Specimen specimen;

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

  public Donor withStudyId(String studyId) {
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

  public Donor withDonorId(String donorId) {
    this.donorId = donorId;
    return this;
  }

  @JsonProperty("donorSubmitterId")
  public String getDonorSubmitterId() {
    return donorSubmitterId;
  }

  @JsonProperty("donorSubmitterId")
  public void setDonorSubmitterId(String donorSubmitterId) {
    this.donorSubmitterId = donorSubmitterId;
  }

  public Donor withDonorSubmitterId(String donorSubmitterId) {
    this.donorSubmitterId = donorSubmitterId;
    return this;
  }

  @JsonProperty("donorGender")
  public DonorGender getDonorGender() {
    return donorGender;
  }

  @JsonProperty("donorGender")
  public void setDonorGender(DonorGender donorGender) {
    this.donorGender = donorGender;
  }

  public Donor withDonorGender(DonorGender donorGender) {
    this.donorGender = donorGender;
    return this;
  }

  @JsonProperty("specimens")
  public Collection<Specimen> getSpecimens() {
    return specimens;
  }

  public void addSpecimen(Specimen specimen) {
    specimens.add(specimen);
  }

  @JsonProperty("specimens")
  public void setSpecimens(Collection<Specimen> specimens) {
    if (specimens != null) {
      this.specimens = specimens;
    }
  }

  public Donor withSpecimens(Collection<Specimen> specimens) {
    if (specimens != null) {
      this.specimens = specimens;
    }
    return this;
  }
  
  @JsonProperty("specimen")
  public Specimen getSpecimen() {
    return specimen;
  }

  @JsonProperty("specimen")
  public void setSpecimen(Specimen specimen) {
    this.specimen = specimen;
  }

  public Donor withSpecimen(Specimen specimen) {
    this.specimen = specimen;
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

  public Donor withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public void propagateKeys() {
    if (specimen != null) {
      specimen.setStudyId(studyId);
      specimen.setDonorId(donorId);
    }
    /*
    if (specimens != null) {
      for (val s : specimens) {
        s.setStudyId(studyId);
        s.setDonorId(donorId);
      }
    }
    */
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((donorGender == null) ? 0 : donorGender.hashCode());
    result = prime * result + ((donorId == null) ? 0 : donorId.hashCode());
    result = prime * result + ((donorSubmitterId == null) ? 0 : donorSubmitterId.hashCode());
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
    Donor other = (Donor) obj;
    if (donorGender != other.donorGender)
      return false;
    if (donorId == null) {
      if (other.donorId != null)
        return false;
    }
    else if (!donorId.equals(other.donorId))
      return false;
    if (donorSubmitterId == null) {
      if (other.donorSubmitterId != null)
        return false;
    }
    else if (!donorSubmitterId.equals(other.donorSubmitterId))
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
