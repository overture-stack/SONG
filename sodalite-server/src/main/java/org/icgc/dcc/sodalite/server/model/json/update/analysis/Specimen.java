
package org.icgc.dcc.sodalite.server.model.json.update.analysis;

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
    "sample"
})
public class Specimen {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("specimenId")
    private String specimenId;
    @JsonProperty("sample")
    private Sample sample;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("specimenId")
    public String getSpecimenId() {
        return specimenId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("specimenId")
    public void setSpecimenId(String specimenId) {
        this.specimenId = specimenId;
    }

    public Specimen withSpecimenId(String specimenId) {
        this.specimenId = specimenId;
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
