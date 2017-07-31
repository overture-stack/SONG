package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;

@EqualsAndHashCode(callSuper=false)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SequencingReadAnalysis extends Analysis {
    SequencingRead experiment;
    @JsonGetter
    public String getAnalysisType() {
        return "sequencingRead";
    }

    public static SequencingReadAnalysis create(String id, String study, String submitter_id, String state ) {
        val s = new SequencingReadAnalysis();
        s.setAnalysisId(id);
        s.setStudy(study);
        s.setAnalysisSubmitterId(submitter_id);
        s.setAnalysisState(state);
        return s;
    }

}
