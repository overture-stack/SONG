
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
    "variantCall",
    "study"
})
public class VariantCallUpdate {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("apiVersion")
    private String apiVersion;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("submissionId")
    private String submissionId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCall")
    private VariantCall variantCall;
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

    public VariantCallUpdate withApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("submissionId")
    public String getSubmissionId() {
        return submissionId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("submissionId")
    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public VariantCallUpdate withSubmissionId(String submissionId) {
        this.submissionId = submissionId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCall")
    public VariantCall getVariantCall() {
        return variantCall;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("variantCall")
    public void setVariantCall(VariantCall variantCall) {
        this.variantCall = variantCall;
    }

    public VariantCallUpdate withVariantCall(VariantCall variantCall) {
        this.variantCall = variantCall;
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

    public VariantCallUpdate withStudy(Study study) {
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

    public VariantCallUpdate withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
