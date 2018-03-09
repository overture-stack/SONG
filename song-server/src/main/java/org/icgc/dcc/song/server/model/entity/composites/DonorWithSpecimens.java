package org.icgc.dcc.song.server.model.entity.composites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Donor;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper=true)
@Value
public class DonorWithSpecimens extends Donor {
    private List<SpecimenWithSamples> specimens = new ArrayList<>();

    @JsonIgnore
    public void setDonor(Donor d) {
        this.setDonorId(d.getDonorId());
        this.setDonorSubmitterId(d.getDonorSubmitterId());
        this.setStudyId(d.getStudyId());
        this.setDonorGender(d.getDonorGender());
        this.setDonorSubmitterId(d.getDonorSubmitterId());
        this.addInfo(d.getInfoAsString());
    }

    @JsonIgnore
    public Donor createDonor() {
        val donor = Donor.create(getDonorId(),getDonorSubmitterId(),getStudyId(), getDonorGender());
        donor.setInfo(getInfoAsString());
        return donor;
    }

    public void setSpecimens(List<SpecimenWithSamples> s) {
        specimens.clear();
        specimens.addAll(s);
    }

}
