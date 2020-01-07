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
import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.annotation.JsonInclude;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Entity
@Table(name = TableNames.SAMPLE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Sample extends Metadata {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String sampleId;

  @Column(name = TableAttributeNames.SPECIMEN_ID, nullable = false)
  private String specimenId;

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String submitterSampleId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Column(name = TableAttributeNames.MATCHED_NORMAL_SUBMITTER_SAMPLE_ID, nullable = true)
  private String matchedNormalSubmitterSampleId;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String sampleType;

  public void setWithSample(@NonNull Sample u) {
    setSampleId(u.getSampleId());
    setMatchedNormalSubmitterSampleId(u.getMatchedNormalSubmitterSampleId());
    setSubmitterSampleId(u.getSubmitterSampleId());
    setSampleType(u.getSampleType());
    setSpecimenId(u.getSpecimenId());
    setInfo(u.getInfo());
  }
}
