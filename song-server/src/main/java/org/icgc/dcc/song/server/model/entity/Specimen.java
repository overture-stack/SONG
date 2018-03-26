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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;

import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Specimen extends Metadata {

  private String specimenId = "";
  private String donorId="";
  private String specimenSubmitterId = "";
  private String specimenClass = "";
  private String specimenType = "";

  public static Specimen create(String id, @NonNull String submitterId, String donorId, String specimenClass,
                                String type) {
    val s = new Specimen();
    s.setSpecimenId(id);
    s.setDonorId(donorId);
    s.setSpecimenSubmitterId(submitterId);
    s.setSpecimenClass(specimenClass);
    s.setSpecimenType(type);

    return s;
  }

  public void setSpecimenClass(String specimenClass) {
    validate(SPECIMEN_CLASS, specimenClass);
    this.specimenClass = specimenClass;
  }

  public void setSpecimenType(String type) {
    validate(SPECIMEN_TYPE, type);
    specimenType = type;
  }

}
