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

package bio.overture.song.server.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import bio.overture.song.server.model.Metadata;
import bio.overture.song.server.model.enums.ModelAttributeNames;
import bio.overture.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static bio.overture.song.server.model.enums.Constants.DONOR_GENDER;
import static bio.overture.song.server.model.enums.Constants.validate;
import static bio.overture.song.server.model.enums.TableNames.DONOR;

@Entity
@Table(name = DONOR)
@Data
@Builder
@RequiredArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({
    ModelAttributeNames.DONOR_ID,
    ModelAttributeNames.DONOR_SUBMITTER_ID,
    ModelAttributeNames.STUDY_ID,
    ModelAttributeNames.DONOR_GENDER,
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
  private String donorSubmitterId;

  @Column(name = TableAttributeNames.GENDER, nullable = true)
  private String donorGender;

  //NOTE: Since the donorGender field is validated upon setting it, using Lomboks default Builder when
  //  the @AllArgsConstructor is used will by pass the validation since the Builder uses the All Arg Constructor.
  // By using the setter inside the constructor, the building of a Donor will always be validated
  public Donor(String donorId, String studyId, String donorSubmitterId, String donorGender) {
    this.donorId = donorId;
    this.studyId = studyId;
    this.donorSubmitterId = donorSubmitterId;
    setDonorGender(donorGender);
  }

  public void setDonorGender(String gender) {
    validate(DONOR_GENDER, gender);
    this.donorGender = gender;
  }

  public void setWithDonor(@NonNull Donor donorUpdate){
    setDonorSubmitterId(donorUpdate.getDonorSubmitterId());
    setDonorGender(donorUpdate.getDonorGender());
    setDonorId(donorUpdate.getDonorId());
    setInfo(donorUpdate.getInfo());
  }

}
