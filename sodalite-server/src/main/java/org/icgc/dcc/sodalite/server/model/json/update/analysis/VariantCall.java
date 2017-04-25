
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
    "analysisId",
    "variantCallingTool",
    "tumourSampleSubmitterId",
    "tumourSampleId",
    "matchedNormalSampleSubmitterId",
    "matchedNormalSampleId"
})
public class VariantCall {

    @JsonProperty("analysisId")
    private String analysisId;
    @JsonProperty("variantCallingTool")
    private String variantCallingTool;
    @JsonProperty("tumourSampleSubmitterId")
    private String tumourSampleSubmitterId;
    @JsonProperty("tumourSampleId")
    private String tumourSampleId;
    @JsonProperty("matchedNormalSampleSubmitterId")
    private String matchedNormalSampleSubmitterId;
    @JsonProperty("matchedNormalSampleId")
    private String matchedNormalSampleId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("analysisId")
    public String getAnalysisId() {
        return analysisId;
    }

    @JsonProperty("analysisId")
    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public VariantCall withAnalysisId(String analysisId) {
        this.analysisId = analysisId;
        return this;
    }

    @JsonProperty("variantCallingTool")
    public String getVariantCallingTool() {
        return variantCallingTool;
    }

    @JsonProperty("variantCallingTool")
    public void setVariantCallingTool(String variantCallingTool) {
        this.variantCallingTool = variantCallingTool;
    }

    public VariantCall withVariantCallingTool(String variantCallingTool) {
        this.variantCallingTool = variantCallingTool;
        return this;
    }

    @JsonProperty("tumourSampleSubmitterId")
    public String getTumourSampleSubmitterId() {
        return tumourSampleSubmitterId;
    }

    @JsonProperty("tumourSampleSubmitterId")
    public void setTumourSampleSubmitterId(String tumourSampleSubmitterId) {
        this.tumourSampleSubmitterId = tumourSampleSubmitterId;
    }

    public VariantCall withTumourSampleSubmitterId(String tumourSampleSubmitterId) {
        this.tumourSampleSubmitterId = tumourSampleSubmitterId;
        return this;
    }

    @JsonProperty("tumourSampleId")
    public String getTumourSampleId() {
        return tumourSampleId;
    }

    @JsonProperty("tumourSampleId")
    public void setTumourSampleId(String tumourSampleId) {
        this.tumourSampleId = tumourSampleId;
    }

    public VariantCall withTumourSampleId(String tumourSampleId) {
        this.tumourSampleId = tumourSampleId;
        return this;
    }

    @JsonProperty("matchedNormalSampleSubmitterId")
    public String getMatchedNormalSampleSubmitterId() {
        return matchedNormalSampleSubmitterId;
    }

    @JsonProperty("matchedNormalSampleSubmitterId")
    public void setMatchedNormalSampleSubmitterId(String matchedNormalSampleSubmitterId) {
        this.matchedNormalSampleSubmitterId = matchedNormalSampleSubmitterId;
    }

    public VariantCall withMatchedNormalSampleSubmitterId(String matchedNormalSampleSubmitterId) {
        this.matchedNormalSampleSubmitterId = matchedNormalSampleSubmitterId;
        return this;
    }

    @JsonProperty("matchedNormalSampleId")
    public String getMatchedNormalSampleId() {
        return matchedNormalSampleId;
    }

    @JsonProperty("matchedNormalSampleId")
    public void setMatchedNormalSampleId(String matchedNormalSampleId) {
        this.matchedNormalSampleId = matchedNormalSampleId;
    }

    public VariantCall withMatchedNormalSampleId(String matchedNormalSampleId) {
        this.matchedNormalSampleId = matchedNormalSampleId;
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

    public VariantCall withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
