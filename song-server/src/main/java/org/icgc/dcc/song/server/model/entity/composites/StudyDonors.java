package org.icgc.dcc.song.server.model.entity.composites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.icgc.dcc.song.server.model.entity.Study;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudyDonors extends Study {
    List<DonorSpecimens> donors=new ArrayList<>();

    @JsonIgnore
    public Study getStudy() {
        return Study.create(getStudyId(), getName(),getOrganization(), getDescription(), getInfo());
    }

    @JsonIgnore
    public void setStudy(Study s) {
        setStudyId(s.getStudyId());
        setName(s.getName());
        setOrganization(s.getOrganization());
        s.setDescription(s.getDescription());
        s.addInfo(s.getInfo());
    }

    public void addDonor(DonorSpecimens d) {
        donors.add(d);
    }

    public void setDonors(List<DonorSpecimens> donors) {
        donors.clear();
        donors.addAll(donors);
    }
}
