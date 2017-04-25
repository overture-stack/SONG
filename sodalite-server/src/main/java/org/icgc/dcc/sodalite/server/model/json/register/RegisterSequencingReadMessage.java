
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
    "sequencingReadSubmission"
})
public class RegisterSequencingReadMessage {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sequencingReadSubmission")
    private SequencingReadSubmission sequencingReadSubmission;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sequencingReadSubmission")
    public SequencingReadSubmission getSequencingReadSubmission() {
        return sequencingReadSubmission;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sequencingReadSubmission")
    public void setSequencingReadSubmission(SequencingReadSubmission sequencingReadSubmission) {
        this.sequencingReadSubmission = sequencingReadSubmission;
    }

    public RegisterSequencingReadMessage withSequencingReadSubmission(SequencingReadSubmission sequencingReadSubmission) {
        this.sequencingReadSubmission = sequencingReadSubmission;
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

    public RegisterSequencingReadMessage withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
