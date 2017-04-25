
package org.icgc.dcc.sodalite.server.model.json.update.analysis;

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
    "analysisId",
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
    @JsonProperty("analysisId")
    private String analysisId;
    @JsonProperty("aligned")
    private boolean aligned;
    @JsonProperty("alignmentTool")
    private String alignmentTool;
    @JsonProperty("insertSize")
    private int insertSize;
    @JsonProperty("libraryStrategy")
    private SequencingRead.LibraryStrategy libraryStrategy;
    @JsonProperty("pairedEnd")
    private boolean pairedEnd;
    @JsonProperty("referenceGenomeFiles")
    private List<String> referenceGenomeFiles = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("analysisId")
    public String getAnalysisId() {
        return analysisId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("analysisId")
    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public SequencingRead withAnalysisId(String analysisId) {
        this.analysisId = analysisId;
        return this;
    }

    @JsonProperty("aligned")
    public boolean isAligned() {
        return aligned;
    }

    @JsonProperty("aligned")
    public void setAligned(boolean aligned) {
        this.aligned = aligned;
    }

    public SequencingRead withAligned(boolean aligned) {
        this.aligned = aligned;
        return this;
    }

    @JsonProperty("alignmentTool")
    public String getAlignmentTool() {
        return alignmentTool;
    }

    @JsonProperty("alignmentTool")
    public void setAlignmentTool(String alignmentTool) {
        this.alignmentTool = alignmentTool;
    }

    public SequencingRead withAlignmentTool(String alignmentTool) {
        this.alignmentTool = alignmentTool;
        return this;
    }

    @JsonProperty("insertSize")
    public int getInsertSize() {
        return insertSize;
    }

    @JsonProperty("insertSize")
    public void setInsertSize(int insertSize) {
        this.insertSize = insertSize;
    }

    public SequencingRead withInsertSize(int insertSize) {
        this.insertSize = insertSize;
        return this;
    }

    @JsonProperty("libraryStrategy")
    public SequencingRead.LibraryStrategy getLibraryStrategy() {
        return libraryStrategy;
    }

    @JsonProperty("libraryStrategy")
    public void setLibraryStrategy(SequencingRead.LibraryStrategy libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
    }

    public SequencingRead withLibraryStrategy(SequencingRead.LibraryStrategy libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
        return this;
    }

    @JsonProperty("pairedEnd")
    public boolean isPairedEnd() {
        return pairedEnd;
    }

    @JsonProperty("pairedEnd")
    public void setPairedEnd(boolean pairedEnd) {
        this.pairedEnd = pairedEnd;
    }

    public SequencingRead withPairedEnd(boolean pairedEnd) {
        this.pairedEnd = pairedEnd;
        return this;
    }

    @JsonProperty("referenceGenomeFiles")
    public List<String> getReferenceGenomeFiles() {
        return referenceGenomeFiles;
    }

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
