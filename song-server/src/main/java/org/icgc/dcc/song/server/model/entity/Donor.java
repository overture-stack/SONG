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

package org.icgc.dcc.song.server.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static org.icgc.dcc.song.server.model.ModelAttributeNames.DONOR_GENDER_MODEL_ATTR;
import static org.icgc.dcc.song.server.model.ModelAttributeNames.DONOR_ID_MODEL_ATTR;
import static org.icgc.dcc.song.server.model.ModelAttributeNames.DONOR_SUBMITTER_ID_MODEL_ATTR;
import static org.icgc.dcc.song.server.model.ModelAttributeNames.INFO_MODEL_ATTR;
import static org.icgc.dcc.song.server.model.ModelAttributeNames.SPECIMENS_MODEL_ATTR;
import static org.icgc.dcc.song.server.model.ModelAttributeNames.STUDY_ID_MODEL_ATTR;
import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;
import static org.icgc.dcc.song.server.model.enums.TableNames.DONOR;
import static org.icgc.dcc.song.server.repository.TableAttributeNames.GENDER;
import static org.icgc.dcc.song.server.repository.TableAttributeNames.ID;
import static org.icgc.dcc.song.server.repository.TableAttributeNames.SUBMITTER_ID;

@Entity
@Table(name = DONOR)
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@JsonPropertyOrder({
    DONOR_ID_MODEL_ATTR,
    DONOR_SUBMITTER_ID_MODEL_ATTR,
    STUDY_ID_MODEL_ATTR,
    DONOR_GENDER_MODEL_ATTR,
    SPECIMENS_MODEL_ATTR,
    INFO_MODEL_ATTR })
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Donor extends Metadata {

  @Id
  @Column(name = ID, updatable = false, unique = true, nullable = false)
  private String donorId = "";

  @Column(name = STUDY_ID_MODEL_ATTR)
  private String studyId = "";

  @Column(name = SUBMITTER_ID)
  private String donorSubmitterId = "";

  @Column(name = GENDER)
  private String donorGender = "";

  public static Donor create(String id, String submitterId, String studyId, String gender) {
    val d = new Donor();
    d.setDonorId(id);
    d.setStudyId(studyId);
    d.setDonorSubmitterId(submitterId);
    d.setDonorGender(gender);
    return d;
  }

  //RTISMA_TODO: remove this, should have its own validation. Need gender to be null so can create hibernate examples for
  // finding entities. This would servce as a data and request entity
  public void setDonorGender(String gender) {
    validate(DONOR_GENDER, gender);
    this.donorGender = gender;
  }

}
