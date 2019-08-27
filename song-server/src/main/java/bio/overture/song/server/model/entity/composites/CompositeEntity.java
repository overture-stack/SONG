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
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
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
