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

package bio.overture.song.server.model.entity.composites;

import bio.overture.song.server.model.entity.Donor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.val;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DonorWithSpecimens extends Donor {
  private List<SpecimenWithSamples> specimens = new ArrayList<>();

  @JsonIgnore
  public void setDonor(Donor d) {
    this.setDonorId(d.getDonorId());
    this.setSubmitterDonorId(d.getSubmitterDonorId());
    this.setStudyId(d.getStudyId());
    this.setDonorGender(d.getDonorGender());
    this.setSubmitterDonorId(d.getSubmitterDonorId());
    this.addInfo(d.getInfoAsString());
  }

  @JsonIgnore
  public Donor createDonor() {
    val donor =
        Donor.builder()
            .donorId(getDonorId())
            .submitterDonorId(getSubmitterDonorId())
            .donorGender(getDonorGender())
            .studyId(getStudyId())
            .build();
    donor.setInfo(getInfoAsString());
    return donor;
  }

  public void setSpecimens(List<SpecimenWithSamples> s) {
    specimens.clear();
    specimens.addAll(s);
  }
}
