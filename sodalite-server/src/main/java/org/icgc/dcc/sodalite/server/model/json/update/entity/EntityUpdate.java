
package org.icgc.dcc.sodalite.server.model.json.update.entity;

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
    "apiVersion",
    "submissionId",
    "donors",
    "specimens",
    "samples"
})
public class EntityUpdate {

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
    @JsonProperty("donors")
    private List<Donor> donors = null;
    @JsonProperty("specimens")
    private List<Specimens> specimens = null;
    @JsonProperty("samples")
    private List<Sample> samples = null;
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

    public EntityUpdate withApiVersion(String apiVersion) {
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

    public EntityUpdate withSubmissionId(String submissionId) {
        this.submissionId = submissionId;
        return this;
    }

    @JsonProperty("donors")
    public List<Donor> getDonors() {
        return donors;
    }

    @JsonProperty("donors")
    public void setDonors(List<Donor> donors) {
        this.donors = donors;
    }

    public EntityUpdate withDonors(List<Donor> donors) {
        this.donors = donors;
        return this;
    }

    @JsonProperty("specimens")
    public List<Specimens> getSpecimens() {
        return specimens;
    }

    @JsonProperty("specimens")
    public void setSpecimens(List<Specimens> specimens) {
        this.specimens = specimens;
    }

    public EntityUpdate withSpecimens(List<Specimens> specimens) {
        this.specimens = specimens;
        return this;
    }

    @JsonProperty("samples")
    public List<Sample> getSamples() {
        return samples;
    }

    @JsonProperty("samples")
    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    public EntityUpdate withSamples(List<Sample> samples) {
        this.samples = samples;
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

    public EntityUpdate withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
