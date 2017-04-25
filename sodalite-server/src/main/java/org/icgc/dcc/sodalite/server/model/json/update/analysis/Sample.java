
package org.icgc.dcc.sodalite.server.model.json.update.analysis;

import java.util.HashMap;
import java.util.List;
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
    "sampleId",
    "files"
})
public class Sample {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sampleId")
    private String sampleId;
    @JsonProperty("files")
    private List<File> files = null;
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

    @JsonProperty("files")
    public List<File> getFiles() {
        return files;
    }

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

}
