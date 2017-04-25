
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
    "variantCallSubmission"
})
public class RegisterVariantCallMessage {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCallSubmission")
    private VariantCallSubmission variantCallSubmission;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCallSubmission")
    public VariantCallSubmission getVariantCallSubmission() {
        return variantCallSubmission;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCallSubmission")
    public void setVariantCallSubmission(VariantCallSubmission variantCallSubmission) {
        this.variantCallSubmission = variantCallSubmission;
    }

    public RegisterVariantCallMessage withVariantCallSubmission(VariantCallSubmission variantCallSubmission) {
        this.variantCallSubmission = variantCallSubmission;
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

    public RegisterVariantCallMessage withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
