
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
    "aligned",
    "alignmentTool",
    "insertSize",
    "libraryStrategy",
    "pairedEnd",
    "referenceGenomeFiles"
})
public class SequencingRead {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("aligned")
    private boolean aligned;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("alignmentTool")
    private String alignmentTool;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("insertSize")
    private int insertSize;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("libraryStrategy")
    private SequencingRead.LibraryStrategy libraryStrategy;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("pairedEnd")
    private boolean pairedEnd;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("referenceGenomeFiles")
    private List<String> referenceGenomeFiles = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("aligned")
    public boolean isAligned() {
        return aligned;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("aligned")
    public void setAligned(boolean aligned) {
        this.aligned = aligned;
    }

    public SequencingRead withAligned(boolean aligned) {
        this.aligned = aligned;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("alignmentTool")
    public String getAlignmentTool() {
        return alignmentTool;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("alignmentTool")
    public void setAlignmentTool(String alignmentTool) {
        this.alignmentTool = alignmentTool;
    }

    public SequencingRead withAlignmentTool(String alignmentTool) {
        this.alignmentTool = alignmentTool;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("insertSize")
    public int getInsertSize() {
        return insertSize;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("insertSize")
    public void setInsertSize(int insertSize) {
        this.insertSize = insertSize;
    }

    public SequencingRead withInsertSize(int insertSize) {
        this.insertSize = insertSize;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("libraryStrategy")
    public SequencingRead.LibraryStrategy getLibraryStrategy() {
        return libraryStrategy;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("libraryStrategy")
    public void setLibraryStrategy(SequencingRead.LibraryStrategy libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
    }

    public SequencingRead withLibraryStrategy(SequencingRead.LibraryStrategy libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("pairedEnd")
    public boolean isPairedEnd() {
        return pairedEnd;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("pairedEnd")
    public void setPairedEnd(boolean pairedEnd) {
        this.pairedEnd = pairedEnd;
    }

    public SequencingRead withPairedEnd(boolean pairedEnd) {
        this.pairedEnd = pairedEnd;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("referenceGenomeFiles")
    public List<String> getReferenceGenomeFiles() {
        return referenceGenomeFiles;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("referenceGenomeFiles")
    public void setReferenceGenomeFiles(List<String> referenceGenomeFiles) {
        this.referenceGenomeFiles = referenceGenomeFiles;
    }

    public SequencingRead withReferenceGenomeFiles(List<String> referenceGenomeFiles) {
        this.referenceGenomeFiles = referenceGenomeFiles;
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

    public SequencingRead withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    public enum LibraryStrategy {

        WGS("WGS"),
        WXS("WXS"),
        RNA_SEQ("RNA-Seq"),
        CH_IP_SEQ("ChIP-Seq"),
        MI_RNA_SEQ("miRNA-Seq"),
        BISULFITE_SEQ("Bisulfite-Seq"),
        VALIDATION("Validation"),
        AMPLICON("Amplicon"),
        OTHER("Other");
        private final String value;
        private final static Map<String, SequencingRead.LibraryStrategy> CONSTANTS = new HashMap<String, SequencingRead.LibraryStrategy>();

        static {
            for (SequencingRead.LibraryStrategy c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private LibraryStrategy(String value) {
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
        public static SequencingRead.LibraryStrategy fromValue(String value) {
            SequencingRead.LibraryStrategy constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
