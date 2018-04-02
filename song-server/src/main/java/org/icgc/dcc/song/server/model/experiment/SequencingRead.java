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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

import static org.icgc.dcc.song.server.model.enums.Constants.LIBRARY_STRATEGY;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@EqualsAndHashCode(callSuper=true)
@JsonPropertyOrder({ "analysisId", "aligned", "alignmentTool", "insertSize", "libraryStrategy", "pairedEnd", "referenceGenome", "info" })
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@ToString(callSuper = true)
@Data
public class SequencingRead extends Experiment {
  private String analysisId = "";
  private Boolean aligned;
  private String alignmentTool;
  private Long insertSize;
  private String libraryStrategy;
  private Boolean pairedEnd;
  private String referenceGenome;

  public static SequencingRead create(String id, Boolean aligned, String tool, Long size, String strategy,
                               Boolean isPaired, String genome) {
    val s = new SequencingRead();
    s.setAnalysisId(id);
    s.setAligned(aligned);
    s.setAlignmentTool(tool);
    s.setInsertSize(size);
    s.setLibraryStrategy(strategy);
    s.setPairedEnd(isPaired);
    s.setReferenceGenome(genome);

    return s;
  }

  public void setLibraryStrategy(String strategy) {
    validate(LIBRARY_STRATEGY, strategy);
    libraryStrategy = strategy;
  }

}
