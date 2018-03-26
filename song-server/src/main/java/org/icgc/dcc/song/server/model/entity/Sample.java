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
import org.icgc.dcc.song.server.model.enums.Constants;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Sample extends Metadata {

  private String sampleId = "";
  private String specimenId="";
  private String sampleSubmitterId = "";
  private String sampleType = "";

  public static Sample create(String id, @NonNull String submitter, String specimenId, String type) {
    val sample = new Sample();
    sample.setSampleId(id);
    sample.setSpecimenId(specimenId);
    sample.setSampleSubmitterId(submitter);

    sample.setSampleType(type);

    return sample;
  }

  public void setSampleType(String type) {
    Constants.validate(Constants.SAMPLE_TYPE, type);
    sampleType = type;
  }


}
