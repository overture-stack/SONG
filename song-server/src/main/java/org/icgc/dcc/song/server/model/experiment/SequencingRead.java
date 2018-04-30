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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static org.icgc.dcc.song.server.model.enums.Constants.LIBRARY_STRATEGY;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@Entity
@Table(name = TableNames.SEQUENCINGREAD)
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper=true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonPropertyOrder({
    ModelAttributeNames.ANALYSIS_ID,
    ModelAttributeNames.ALIGNED,
    ModelAttributeNames.ALIGNMENT_TOOL,
    ModelAttributeNames.INSERT_SIZE,
    ModelAttributeNames.LIBRARY_STRATEGY,
    ModelAttributeNames.PAIRED_END,
    ModelAttributeNames.REFERENCE_GENOME,
    ModelAttributeNames.INFO
})
public class SequencingRead extends Experiment {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String analysisId = "";

  @Column(name = TableAttributeNames.ALIGNED, nullable = true)
  private Boolean aligned;

  @Column(name = TableAttributeNames.ALIGNMENT_TOOL, nullable = true)
  private String alignmentTool;

  @Column(name = TableAttributeNames.INSERT_SIZE, nullable = true)
  private Long insertSize;

  @Column(name = TableAttributeNames.LIBRARY_STRATEGY, nullable = false)
  private String libraryStrategy;

  @Column(name = TableAttributeNames.PAIRED_END, nullable = true)
  private Boolean pairedEnd;

  @Column(name = TableAttributeNames.REFERENCE_GENOME, nullable = true)
  private String referenceGenome;

  public void setLibraryStrategy(String strategy) {
    validate(LIBRARY_STRATEGY, strategy);
    libraryStrategy = strategy;
  }

}
