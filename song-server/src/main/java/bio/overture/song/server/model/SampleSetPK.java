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

package bio.overture.song.server.model;

import bio.overture.song.server.model.enums.TableAttributeNames;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Data;
import lombok.NonNull;
import lombok.val;

@Embeddable
@Data
public class SampleSetPK implements Serializable {

  @Column(name = TableAttributeNames.ANALYSIS_ID, nullable = false)
  private String analysisId;

  @Column(name = TableAttributeNames.SAMPLE_ID, nullable = false)
  private String sampleId;

  public static SampleSetPK createSampleSetPK(
      @NonNull String analysisId, @NonNull String sampleId) {
    val s = new SampleSetPK();
    s.setAnalysisId(analysisId);
    s.setSampleId(sampleId);
    return s;
  }
}
