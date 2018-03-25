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
