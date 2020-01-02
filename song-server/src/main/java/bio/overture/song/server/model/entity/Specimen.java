/*
 * Copyright (c) 2018-2019. Ontario Institute for Cancer Research
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

package bio.overture.song.server.model.entity;

import bio.overture.song.core.model.Metadata;
import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.annotation.JsonInclude;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Entity
@Table(name = TableNames.SPECIMEN)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Specimen extends Metadata {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String specimenId;

  @Column(name = TableAttributeNames.DONOR_ID, nullable = false)
  private String donorId;

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String submitterSpecimenId;

  @Column(name = TableAttributeNames.TUMOUR_NORMAL_DESIGNATION, nullable = false)
  private String tumourNormalDesignation;

  @Column(name = TableAttributeNames.TISSUE_SOURCE, nullable = false)
  private String specimenTissueSource;

  public void setWithSpecimen(@NonNull Specimen specimenUpdate) {
    setSubmitterSpecimenId(specimenUpdate.getSubmitterSpecimenId());
    setDonorId(specimenUpdate.getDonorId());
    setSpecimenId(specimenUpdate.getSpecimenId());
    setTumourNormalDesignation(specimenUpdate.getTumourNormalDesignation());
    setSpecimenTissueSource(specimenUpdate.getSpecimenTissueSource());
    setInfo(specimenUpdate.getInfo());
  }
}
