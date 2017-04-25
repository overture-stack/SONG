
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
    "donorId",
    "specimen"
})
public class Donor {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donorId")
    private String donorId;
    @JsonProperty("specimen")
    private Specimen specimen;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donorId")
    public String getDonorId() {
        return donorId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donorId")
    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public Donor withDonorId(String donorId) {
        this.donorId = donorId;
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

}
