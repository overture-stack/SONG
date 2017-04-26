package org.icgc.dcc.sodalite.server.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"donorId",
    "donorSubmitterId",
    "donorGender",
    "specimens"
})
public class Donor implements Entity {

  @JsonProperty("donorId")
  private String donorId;

  @JsonProperty("donorSubmitterId")
  private String donorSubmitterId;

  @JsonProperty("donorGender")
  private DonorGender donorGender;

  @JsonProperty("specimens")
  private Collection<Specimen> specimens;
  
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
      this.specimens = specimens;
  }

  public Donor withSpecimens(Collection<Specimen> specimens) {
      this.specimens = specimens;
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
}
