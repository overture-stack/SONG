
package org.icgc.dcc.sodalite.server.model;

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
		"sampleId",
    "sampleSubmitterId",
    "sampleType",
    "files"
})
public class Sample {

  @JsonProperty("sampleId")
  private String sampleId;

  @JsonProperty("sampleSubmitterId")
  private String sampleSubmitterId;

  @JsonProperty("sampleType")
  private SampleType sampleType;
  
  @JsonProperty("files")
  private List<File> files = null;
  
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("sampleId")
  public String getSampleId() {
      return sampleId;
  }

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
  public SampleType getSampleType() {
      return sampleType;
  }

  @JsonProperty("sampleType")
  public void setSampleType(SampleType sampleType) {
      this.sampleType = sampleType;
  }

  public Sample withSampleType(SampleType sampleType) {
      this.sampleType = sampleType;
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
