package org.icgc.dcc.song.server.model.entity.composites;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;

@EqualsAndHashCode(callSuper=true)
@Data
@ToString(callSuper = true)
public class CompositeEntity extends Sample {
    private Specimen specimen;
    private Donor donor;

    // TODO: Check out Lombok @Builder annotations
    public static CompositeEntity create(Sample sample) {
        val s = new CompositeEntity();

        s.setSampleId(sample.getSampleId());
        s.setSampleSubmitterId(sample.getSampleSubmitterId());
        s.setSampleType(sample.getSampleType());
        s.setInfo(sample.getInfoAsString());
        s.setSpecimenId(sample.getSpecimenId());

        return s;
    }
}
