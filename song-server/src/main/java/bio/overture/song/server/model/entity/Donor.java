/*
 * Copyright (c) 2018 - 2019. Ontario Institute for Cancer Research
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
import bio.overture.song.server.model.enums.ModelAttributeNames;
import bio.overture.song.server.model.enums.TableAttributeNames;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static bio.overture.song.server.model.enums.TableNames.DONOR;

@Entity
@Table(name = DONOR)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({
  ModelAttributeNames.DONOR_ID,
  ModelAttributeNames.DONOR_SUBMITTER_ID,
  ModelAttributeNames.STUDY_ID,
  ModelAttributeNames.GENDER,
  ModelAttributeNames.SPECIMENS,
  ModelAttributeNames.INFO
})
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Donor extends Metadata {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String donorId;

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String submitterDonorId;

  @Column(name = TableAttributeNames.GENDER, nullable = false)
  private String gender;

  public void setWithDonor(@NonNull Donor donorUpdate) {
    setSubmitterDonorId(donorUpdate.getSubmitterDonorId());
    setGender(donorUpdate.getGender());
    setDonorId(donorUpdate.getDonorId());
    setInfo(donorUpdate.getInfo());
  }
}
