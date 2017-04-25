
package org.icgc.dcc.sodalite.server.model.json.register;

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
    "fileName",
    "fileSize",
    "fileType",
    "fileMd5"
})
public class File {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileName")
    private String fileName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileSize")
    private int fileSize;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    private File.FileType fileType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileMd5")
    private String fileMd5;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileName")
    public String getFileName() {
        return fileName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileName")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileSize")
    public int getFileSize() {
        return fileSize;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileSize")
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public File withFileSize(int fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    public File.FileType getFileType() {
        return fileType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    public void setFileType(File.FileType fileType) {
        this.fileType = fileType;
    }

    public File withFileType(File.FileType fileType) {
        this.fileType = fileType;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileMd5")
    public String getFileMd5() {
        return fileMd5;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileMd5")
    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public File withFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
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

    public enum FileType {

        FASTA("FASTA"),
        FAI("FAI"),
        FASTQ("FASTQ"),
        BAM("BAM"),
        BAI("BAI"),
        VCF("VCF"),
        TBI("TBI"),
        IDX("IDX"),
        XML("XML");
        private final String value;
        private final static Map<String, File.FileType> CONSTANTS = new HashMap<String, File.FileType>();

        static {
            for (File.FileType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private FileType(String value) {
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
        public static File.FileType fromValue(String value) {
            File.FileType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
