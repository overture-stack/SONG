
package org.icgc.dcc.sodalite.server.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "studyId", "sampleId", "objectId", "fileName", "fileSize", "fileType", "fileMd5", "metadataDoc"
})

public class File extends AbstractEntity {

  @JsonProperty("studyId")
  private String studyId;

  @JsonProperty("sampleId")
  private String sampleId;

  @JsonProperty("objectId")
  private String objectId;

  @JsonProperty("fileName")
  private String fileName;

  @JsonProperty("fileSize")
  private long fileSize;

  @JsonProperty("fileType")
  private FileType fileType;

  @JsonProperty("fileMd5")
  private String fileMd5;

  @JsonProperty("metadataDoc")
  private String metadataDoc;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("studyId")
  public String getStudyId() {
    return studyId;
  }

  @JsonProperty("studyId")
  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public File withStudyId(String studyId) {
    this.studyId = studyId;
    return this;
  }

  @JsonProperty("sampleId")
  public String getSampleId() {
    return sampleId;
  }

  @JsonProperty("sampleId")
  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  public File withSampleId(String sampleId) {
    this.sampleId = sampleId;
    return this;
  }

  @JsonProperty("objectId")
  public String getObjectId() {
    return objectId;
  }

  @JsonProperty("objectId")
  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public File withObjectId(String objectId) {
    this.objectId = objectId;
    return this;
  }

  @JsonProperty("fileName")
  public String getFileName() {
    return fileName;
  }

  @JsonProperty("fileName")
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public File withFileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  @JsonProperty("fileSize")
  public long getFileSize() {
    return fileSize;
  }

  @JsonProperty("fileSize")
  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public File withFileSize(long fileSize) {
    this.fileSize = fileSize;
    return this;
  }

  @JsonProperty("fileType")
  public FileType getFileType() {
    return fileType;
  }

  @JsonProperty("fileType")
  public void setFileType(FileType fileType) {
    this.fileType = fileType;
  }

  public File withFileType(FileType fileType) {
    this.fileType = fileType;
    return this;
  }

  @JsonProperty("fileMd5")
  public String getFileMd5() {
    return fileMd5;
  }

  @JsonProperty("fileMd5")
  public void setFileMd5(String fileMd5) {
    this.fileMd5 = fileMd5;
  }

  public File withFileMd5(String fileMd5) {
    this.fileMd5 = fileMd5;
    return this;
  }

  @JsonProperty("metadataDoc")
  public String getMetadataDoc() {
    return metadataDoc;
  }

  @JsonProperty("metadataDoc")
  public void setMetadataDoc(String metadataDoc) {
    this.metadataDoc = metadataDoc;
  }

  public File withMetadataDoc(String metadataDoc) {
    this.metadataDoc = metadataDoc;
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

  public File withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public void propagateKeys() {
    // no behaviour
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fileMd5 == null) ? 0 : fileMd5.hashCode());
    result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
    result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
    result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
    result = prime * result + ((metadataDoc == null) ? 0 : metadataDoc.hashCode());
    result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
    result = prime * result + ((sampleId == null) ? 0 : sampleId.hashCode());
    result = prime * result + ((studyId == null) ? 0 : studyId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    File other = (File) obj;
    if (fileMd5 == null) {
      if (other.fileMd5 != null) return false;
    } else if (!fileMd5.equals(other.fileMd5)) return false;
    if (fileName == null) {
      if (other.fileName != null) return false;
    } else if (!fileName.equals(other.fileName)) return false;
    if (fileSize != other.fileSize) return false;
    if (fileType != other.fileType) return false;
    if (metadataDoc == null) {
      if (other.metadataDoc != null) return false;
    } else if (!metadataDoc.equals(other.metadataDoc)) return false;
    if (objectId == null) {
      if (other.objectId != null) return false;
    } else if (!objectId.equals(other.objectId)) return false;
    if (sampleId == null) {
      if (other.sampleId != null) return false;
    } else if (!sampleId.equals(other.sampleId)) return false;
    if (studyId == null) {
      if (other.studyId != null) return false;
    } else if (!studyId.equalsIgnoreCase(other.studyId)) return false;
    return true;
  }

}
