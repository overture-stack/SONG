package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.model.enums.Constants;

import java.util.List;

import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonTypeInfo(
        use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property="analysisType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value=SequencingReadAnalysis.class, name="sequencingRead"),
        @JsonSubTypes.Type(value=VariantCallAnalysis.class, name="variantCall")
})
public abstract class Analysis extends Metadata {

    private String analysisId="";
    private String study="";
    private String analysisState = UNPUBLISHED.name();

    private List<CompositeEntity> sample;
    private List<File> file;

    abstract public String getAnalysisType();

    public void setAnalysisState(String state) {
        Constants.validate(Constants.ANALYSIS_STATE, state);
        this.analysisState=state;
    }


}

