package org.icgc.dcc.song.importer.model;

import lombok.Data;

@Data
public class PcawgSampleBean {

  private String donor_unique_id;
  private String donor_wgs_exclusion_white_gray;
  private String submitter_donor_id;
  private String icgc_donor_id;
  private String dcc_project_code;
  private String aliquot_id;
  private String submitter_specimen_id;
  private String icgc_specimen_id;
  private String submitter_sample_id;
  private String icgc_sample_id;
  private String dcc_specimen_type;
  private String library_strategy;

}
