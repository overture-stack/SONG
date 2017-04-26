
package org.icgc.dcc.sodalite.server.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.icgc.dcc.sodalite.server.model.LibraryStrategy;

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
    private LibraryStrategy libraryStrategy;
    @JsonProperty("pairedEnd")
    private boolean pairedEnd;
    @JsonProperty("referenceGenomeFiles")
    private Collection<String> referenceGenomeFiles = null;
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
    public LibraryStrategy getLibraryStrategy() {
        return libraryStrategy;
    }

    @JsonProperty("libraryStrategy")
    public void setLibraryStrategy(LibraryStrategy libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
    }

    public SequencingRead withLibraryStrategy(LibraryStrategy libraryStrategy) {
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
    public Collection<String> getReferenceGenomeFiles() {
        return referenceGenomeFiles;
    }

    @JsonProperty("referenceGenomeFiles")
    public void setReferenceGenomeFiles(Collection<String> referenceGenomeFiles) {
        this.referenceGenomeFiles = referenceGenomeFiles;
    }

    public SequencingRead withReferenceGenomeFiles(Collection<String> referenceGenomeFiles) {
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
}
