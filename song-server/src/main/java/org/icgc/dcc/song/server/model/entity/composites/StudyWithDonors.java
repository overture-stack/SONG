/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.icgc.dcc.song.server.model.entity.composites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Study;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper=true)
@ToString(callSuper = true)
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
        setDescription(s.getDescription());
        setInfo(s.getInfoAsString());
    }

    public void addDonor(DonorWithSpecimens d) {
        donors.add(d);
    }

    public void setDonors(List<DonorWithSpecimens> donors) {
        this.donors.clear();
        this.donors.addAll(donors);
    }

}
