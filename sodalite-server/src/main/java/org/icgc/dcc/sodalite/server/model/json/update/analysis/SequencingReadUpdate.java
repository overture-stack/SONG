
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
    "apiVersion",
    "submissionId",
    "sequencingRead",
    "study"
})
public class SequencingReadUpdate {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    private String apiVersion;
    @JsonProperty("submissionId")
    private String submissionId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sequencingRead")
    private SequencingRead sequencingRead;
    @JsonProperty("study")
    private Study study;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public SequencingReadUpdate withApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @JsonProperty("submissionId")
    public String getSubmissionId() {
        return submissionId;
    }

    @JsonProperty("submissionId")
    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public SequencingReadUpdate withSubmissionId(String submissionId) {
        this.submissionId = submissionId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sequencingRead")
    public SequencingRead getSequencingRead() {
        return sequencingRead;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("sequencingRead")
    public void setSequencingRead(SequencingRead sequencingRead) {
        this.sequencingRead = sequencingRead;
    }

    public SequencingReadUpdate withSequencingRead(SequencingRead sequencingRead) {
        this.sequencingRead = sequencingRead;
        return this;
    }

    @JsonProperty("study")
    public Study getStudy() {
        return study;
    }

    @JsonProperty("study")
    public void setStudy(Study study) {
        this.study = study;
    }

    public SequencingReadUpdate withStudy(Study study) {
        this.study = study;
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

    public SequencingReadUpdate withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
