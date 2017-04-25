
package org.icgc.dcc.sodalite.server.model.json.register;

import java.util.HashMap;
import java.util.List;
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
    "sampleSubmitterId",
    "sampleType",
    "files"
})
public class Sample {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleSubmitterId")
    private String sampleSubmitterId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleType")
    private Sample.SampleType sampleType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("files")
    private List<File> files = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleSubmitterId")
    public String getSampleSubmitterId() {
        return sampleSubmitterId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleSubmitterId")
    public void setSampleSubmitterId(String sampleSubmitterId) {
        this.sampleSubmitterId = sampleSubmitterId;
    }

    public Sample withSampleSubmitterId(String sampleSubmitterId) {
        this.sampleSubmitterId = sampleSubmitterId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleType")
    public Sample.SampleType getSampleType() {
        return sampleType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleType")
    public void setSampleType(Sample.SampleType sampleType) {
        this.sampleType = sampleType;
    }

    public Sample withSampleType(Sample.SampleType sampleType) {
        this.sampleType = sampleType;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("files")
    public List<File> getFiles() {
        return files;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("files")
    public void setFiles(List<File> files) {
        this.files = files;
    }

    public Sample withFiles(List<File> files) {
        this.files = files;
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
