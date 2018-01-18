package org.icgc.dcc.song.server.model.entity.composites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Study;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper=false)
@Data
public class StudyWithDonors extends Study {

    List<DonorWithSpecimens> donors=new ArrayList<>();

    @JsonIgnore
    public Study getStudy() {
        val s= Study.create(getStudyId(), getName(),getOrganization(), getDescription());
        s.setInfo(getInfoAsString());
        return s;
    }

    @JsonIgnore
    public void setStudy(Study s) {
        setStudyId(s.getStudyId());
        setName(s.getName());
        setOrganization(s.getOrganization());
        s.setDescription(s.getDescription());
        s.addInfo(s.getInfoAsString());
    }

    public void addDonor(DonorWithSpecimens d) {
        donors.add(d);
    }

    public void setDonors(List<DonorWithSpecimens> donors) {
        this.donors.clear();
        this.donors.addAll(donors);
    }

}
