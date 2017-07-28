package org.icgc.dcc.song.server.model.entity.composites;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;

import java.util.ArrayList;
import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Value
public class SpecimenWithSamples extends Specimen {
    private List<Sample> samples = new ArrayList<>();

    public void setSpecimen(Specimen s) {
        setSpecimenId(s.getSpecimenId());
        setDonorId(s.getDonorId());
        setSpecimenSubmitterId(s.getSpecimenSubmitterId());
        setSpecimenClass(s.getSpecimenClass());
        setSpecimenType(s.getSpecimenType());

        addInfo(s.getInfo());
    }

    public Specimen getSpecimen() {
        val s= Specimen.create(getSpecimenId(),getDonorId(),getSpecimenSubmitterId(), getSpecimenClass(),
                getSpecimenType());
        s.setInfo(getInfo());
        return s;
    }

    public void addSample(Sample s) {
        samples.add(s);
    }

    public void setSamples(List<Sample> s) {
        samples.clear();
        samples.addAll(s);
    }
}
