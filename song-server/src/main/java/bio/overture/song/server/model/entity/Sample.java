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

import static bio.overture.song.server.model.enums.Constants.SAMPLE_TYPE;
import static bio.overture.song.server.model.enums.Constants.validate;

import bio.overture.song.server.model.Metadata;
import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.annotation.JsonInclude;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = TableNames.SAMPLE)
@Data
@Builder
@RequiredArgsConstructor
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
  private String sampleSubmitterId;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String sampleType;

  public Sample(String sampleId, String specimenId, String sampleSubmitterId, String sampleType) {
    this.sampleId = sampleId;
    this.specimenId = specimenId;
    this.sampleSubmitterId = sampleSubmitterId;
    setSampleType(sampleType);
  }

  public void setSampleType(String type) {
    validate(SAMPLE_TYPE, type);
    sampleType = type;
  }

  public void setWithSample(@NonNull Sample u) {
    setSampleId(u.getSampleId());
    setSampleSubmitterId(u.getSampleSubmitterId());
    setSampleType(u.getSampleType());
    setSpecimenId(u.getSpecimenId());
    setInfo(u.getInfo());
  }
}
