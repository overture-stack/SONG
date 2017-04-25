
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
    "variantCallUpdate"
})
public class VariantCallUpdateMessage {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCallUpdate")
    private VariantCallUpdate variantCallUpdate;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCallUpdate")
    public VariantCallUpdate getVariantCallUpdate() {
        return variantCallUpdate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCallUpdate")
    public void setVariantCallUpdate(VariantCallUpdate variantCallUpdate) {
        this.variantCallUpdate = variantCallUpdate;
    }

    public VariantCallUpdateMessage withVariantCallUpdate(VariantCallUpdate variantCallUpdate) {
        this.variantCallUpdate = variantCallUpdate;
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

    public VariantCallUpdateMessage withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
