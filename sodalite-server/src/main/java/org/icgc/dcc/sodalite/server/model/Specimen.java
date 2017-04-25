
package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"specimenId",
    "specimenSubmitterId",
    "specimenClass",
    "specimenType",
    "sample"
})
public class Specimen {

  	@JsonProperty("specimenId")
  	private String specimenId;
  
    @JsonProperty("specimenSubmitterId")
    private String specimenSubmitterId;

    @JsonProperty("specimenClass")
    private SpecimenClass specimenClass;

    @JsonProperty("specimenType")
    private SpecimenType specimenType;

    @JsonProperty("sample")
    private Sample sample;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


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
}
