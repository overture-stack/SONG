
package org.icgc.dcc.sodalite.server.model.json.register;

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
    "variantCallingTool",
    "tumourSampleSubmitterId",
    "matchedNormalSampleSubmitterId"
})
public class VariantCall {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCallingTool")
    private String variantCallingTool;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tumourSampleSubmitterId")
    private String tumourSampleSubmitterId;
    @JsonProperty("matchedNormalSampleSubmitterId")
    private String matchedNormalSampleSubmitterId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCallingTool")
    public String getVariantCallingTool() {
        return variantCallingTool;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCallingTool")
    public void setVariantCallingTool(String variantCallingTool) {
        this.variantCallingTool = variantCallingTool;
    }

    public VariantCall withVariantCallingTool(String variantCallingTool) {
        this.variantCallingTool = variantCallingTool;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tumourSampleSubmitterId")
    public String getTumourSampleSubmitterId() {
        return tumourSampleSubmitterId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tumourSampleSubmitterId")
    public void setTumourSampleSubmitterId(String tumourSampleSubmitterId) {
        this.tumourSampleSubmitterId = tumourSampleSubmitterId;
    }

    public VariantCall withTumourSampleSubmitterId(String tumourSampleSubmitterId) {
        this.tumourSampleSubmitterId = tumourSampleSubmitterId;
        return this;
    }

    @JsonProperty("matchedNormalSampleSubmitterId")
    public String getMatchedNormalSampleSubmitterId() {
        return matchedNormalSampleSubmitterId;
    }

    @JsonProperty("matchedNormalSampleSubmitterId")
    public void setMatchedNormalSampleSubmitterId(String matchedNormalSampleSubmitterId) {
        this.matchedNormalSampleSubmitterId = matchedNormalSampleSubmitterId;
    }

    public VariantCall withMatchedNormalSampleSubmitterId(String matchedNormalSampleSubmitterId) {
        this.matchedNormalSampleSubmitterId = matchedNormalSampleSubmitterId;
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

    public VariantCall withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
