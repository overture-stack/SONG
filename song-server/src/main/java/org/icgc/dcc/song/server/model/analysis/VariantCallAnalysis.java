package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.icgc.dcc.song.server.model.experiment.VariantCall;
@EqualsAndHashCode(callSuper=false)
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class VariantCallAnalysis extends Analysis {
    VariantCall experiment;

    @JsonGetter
    public String getAnalysisType() {
        return "variantCall";
    }

    public static VariantCallAnalysis create(String id, String study, String state, String info) {
        val s = new VariantCallAnalysis();
        s.setAnalysisId(id);
        s.setStudy(study);
        s.setAnalysisState(state);
        s.setInfo(info);

        return s;
    }
}
