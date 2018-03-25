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

import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@JsonPropertyOrder({ "donorId", "donorSubmitterId", "studyId", "donorGender", "specimens", "info" })
@JsonInclude(JsonInclude.Include.ALWAYS)

public class Donor extends Metadata {

  private String donorId = "";
  private String donorSubmitterId = "";
  private String studyId = "";
  private String donorGender = "";

  public static Donor create(String id, String submitterId, String studyId, String gender) {
    val d = new Donor();
    d.setDonorId(id);
    d.setStudyId(studyId);
    d.setDonorSubmitterId(submitterId);
    d.setDonorGender(gender);

    return d;
  }

  public void setDonorGender(String gender) {
    validate(DONOR_GENDER, gender);
    this.donorGender = gender;
  }

}
