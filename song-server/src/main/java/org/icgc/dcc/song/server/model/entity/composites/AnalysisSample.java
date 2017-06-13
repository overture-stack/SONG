package org.icgc.dcc.song.server.model.entity.composites;

import lombok.Data;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;

import java.util.List;
@Data
public class AnalysisSample extends Sample {
    private Specimen specimen;
    private Donor donor;

    // TODO: Check out Lombok @Builder annotations
    public static AnalysisSample create(Sample sample) {
        val s = new AnalysisSample();

        s.setSampleId(sample.getSampleId());
        s.setSampleSubmitterId(sample.getSampleSubmitterId());
        s.setSampleType(sample.getSampleType());
        s.setInfo(sample.getInfo());
        s.setSpecimenId(sample.getSpecimenId());

        return s;
    }
}
