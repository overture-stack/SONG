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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Immutable;
import bio.overture.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Immutable
@Table(name = TableNames.BUSINESS_KEY_VIEW)
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class BusinessKeyView {
  public static final String STUDY_ID = "study_id";
  public static final String SPECIMEN_ID = "specimen_id";
  public static final String SPECIMEN_SUBMITTER_ID = "specimen_submitter_id";
  public static final String SAMPLE_ID = "sample_id";
  public static final String SAMPLE_SUBMITTER_ID = "sample_submitter_id";
  public static final String DONOR_ID = "donor_id";
  public static final String DONOR_SUBMITTER_ID = "donor_submitter_id";

  @Id
  @Column(name = STUDY_ID)
  private String studyId;

  @Column(name = SPECIMEN_ID)
  private String specimenId;

  @Column(name = SPECIMEN_SUBMITTER_ID)
  private String specimenSubmitterId;

  @Column(name = SAMPLE_ID)
  private String sampleId;

  @Column(name = SAMPLE_SUBMITTER_ID)
  private String sampleSubmitterId;

}
