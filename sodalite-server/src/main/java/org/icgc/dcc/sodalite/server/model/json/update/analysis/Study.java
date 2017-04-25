
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
    "studyId",
    "donor"
})
public class Study {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("studyId")
    private String studyId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donor")
    private Donor donor;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("studyId")
    public String getStudyId() {
        return studyId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("studyId")
    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public Study withStudyId(String studyId) {
        this.studyId = studyId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donor")
    public Donor getDonor() {
        return donor;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("donor")
    public void setDonor(Donor donor) {
        this.donor = donor;
    }

    public Study withDonor(Donor donor) {
        this.donor = donor;
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

    public Study withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
