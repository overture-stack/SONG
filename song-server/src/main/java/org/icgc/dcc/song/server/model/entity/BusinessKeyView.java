package org.icgc.dcc.song.server.model.entity;

import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Immutable
@Table(name = TableNames.BUSINESS_KEY_VIEW)
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

//  @Column(name = DONOR_ID)
//  private String donorId;
//
//  @Column(name = DONOR_SUBMITTER_ID)
//  private String donorSubmitterId;

}
