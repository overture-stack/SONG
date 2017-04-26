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
	"specimenId",
    "specimenSubmitterId",
    "specimenClass",
    "specimenType",
    "samples"
})
public class Specimen implements Entity {
  	@JsonProperty("specimenId")
  	private String specimenId;
  
    @JsonProperty("specimenSubmitterId")
    private String specimenSubmitterId;

    @JsonProperty("specimenClass")
    private SpecimenClass specimenClass;

    @JsonProperty("specimenType")
    private SpecimenType specimenType;

    @JsonProperty("samples")
    private Collection<Sample> samples;
    
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

    @JsonProperty("samples")
    public Collection<Sample> getSamples() {
        return samples;
    }

    @JsonProperty("samples")
    public void setSamples(Collection<Sample> samples) {
        this.samples = samples;
    }
    
    public void addSample(Sample sample) {
    	samples.add(sample);
    }

    public Specimen withSamples(Collection<Sample> samples) {
        this.samples = samples;
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
