
package org.icgc.dcc.sodalite.server.model.json.update.entity;

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
    "sampleId",
    "sampleSubmitterId",
    "sampleType"
})
public class Sample {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleId")
    private String sampleId;
    @JsonProperty("sampleSubmitterId")
    private String sampleSubmitterId;
    @JsonProperty("sampleType")
    private Sample.SampleType sampleType;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleId")
    public String getSampleId() {
        return sampleId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleId")
    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public Sample withSampleId(String sampleId) {
        this.sampleId = sampleId;
        return this;
    }

    @JsonProperty("sampleSubmitterId")
    public String getSampleSubmitterId() {
        return sampleSubmitterId;
    }

    @JsonProperty("sampleSubmitterId")
    public void setSampleSubmitterId(String sampleSubmitterId) {
        this.sampleSubmitterId = sampleSubmitterId;
    }

    public Sample withSampleSubmitterId(String sampleSubmitterId) {
        this.sampleSubmitterId = sampleSubmitterId;
        return this;
    }

    @JsonProperty("sampleType")
    public Sample.SampleType getSampleType() {
        return sampleType;
    }

    @JsonProperty("sampleType")
    public void setSampleType(Sample.SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public Sample withSampleType(Sample.SampleType sampleType) {
        this.sampleType = sampleType;
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

    public Sample withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    public enum SampleType {

        DNA("DNA"),
        FFPE_DNA("FFPE DNA"),
        AMPLIFIED_DNA("Amplified DNA"),
        RNA("RNA"),
        TOTAL_RNA("Total RNA"),
        FFPE_RNA("FFPE RNA");
        private final String value;
        private final static Map<String, Sample.SampleType> CONSTANTS = new HashMap<String, Sample.SampleType>();

        static {
            for (Sample.SampleType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private SampleType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Sample.SampleType fromValue(String value) {
            Sample.SampleType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
