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
package org.icgc.dcc.song.server.model.experiment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

@EqualsAndHashCode(callSuper=true)
@ToString(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class VariantCall extends Experiment {

  private String analysisId = "";
  private String variantCallingTool;
  private String matchedNormalSampleSubmitterId;

  public static VariantCall create(String id, String tool, String submitterId) {
    val v = new VariantCall();
    v.setAnalysisId(id);
    v.setVariantCallingTool(tool);
    v.setMatchedNormalSampleSubmitterId(submitterId);


    return v;
  }

}
