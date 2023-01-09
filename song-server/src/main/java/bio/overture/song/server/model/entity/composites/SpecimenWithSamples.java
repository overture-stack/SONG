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

import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.val;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SpecimenWithSamples extends Specimen {

  private List<Sample> samples = new ArrayList<>();

  public void setSpecimen(Specimen s) {
    setSpecimenId(s.getSpecimenId());
    setDonorId(s.getDonorId());
    setSubmitterSpecimenId(s.getSubmitterSpecimenId());
    setSpecimenTissueSource(s.getSpecimenTissueSource());
    setTumourNormalDesignation(s.getTumourNormalDesignation());
    setSpecimenType(s.getSpecimenType());

    addInfo(s.getInfoAsString());
  }

  public Specimen getSpecimen() {
    val s =
        Specimen.builder()
            .specimenId(getSpecimenId())
            .submitterSpecimenId(getSubmitterSpecimenId())
            .donorId(getDonorId())
            .tumourNormalDesignation(getTumourNormalDesignation())
            .specimenType(getSpecimenType())
            .specimenTissueSource(getSpecimenTissueSource())
            .build();
    s.setInfo(getInfoAsString());
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
