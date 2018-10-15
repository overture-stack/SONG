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

import bio.overture.song.server.model.enums.TableNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;

import static bio.overture.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

@Entity
@Immutable
@Table(name = TableNames.FULL_VIEW)
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class FullView {

  public static final String FILE_OBJECT_ID																	= "file_object_id";
  public static final String FILE_ACCESS																		= "file_access";
  public static final String FILE_MD5																				= "file_md5";
  public static final String FILE_SIZE																			= "file_size";
  public static final String FILE_NAME																			= "file_name";
  public static final String FILE_TYPE																			= "file_type";
  public static final String FILE_INFO																			= "file_info";
  public static final String SAMPLE_ID																			= "sample_id";
  public static final String SAMPLE_TYPE																		= "sample_type";
  public static final String SAMPLE_SUBMITTER_ID														= "sample_submitter_id";
  public static final String SAMPLE_INFO																		= "sample_info";
  public static final String SPECIMEN_ID																		= "specimen_id";
  public static final String SPECIMEN_TYPE																	= "specimen_type";
  public static final String SPECIMEN_CLASS																	= "specimen_class";
  public static final String SPECIMEN_SUBMITTER_ID													= "specimen_submitter_id";
  public static final String SPECIMEN_INFO																	= "specimen_info";
  public static final String DONOR_ID																				= "donor_id";
  public static final String DONOR_GENDER																		= "donor_gender";
  public static final String DONOR_SUBMITTER_ID															= "donor_submitter_id";
  public static final String DONOR_INFO																			= "donor_info";
  public static final String SEQUENCINGREAD_LIBRARY_STRATEGY								= "sequencingread_library_strategy";
  public static final String SEQUENCINGREAD_ALIGNED													= "sequencingread_aligned";
  public static final String SEQUENCINGREAD_ALIGNMENT_TOOL									= "sequencingread_alignment_tool";
  public static final String SEQUENCINGREAD_INSERT_SIZE											= "sequencingread_insert_size";
  public static final String SEQUENCINGREAD_PAIRED_END											= "sequencingread_paired_end";
  public static final String SEQUENCINGREAD_REFERENCE_GENOME								= "sequencingread_reference_genome";
  public static final String SEQUENCINGREAD_INFO														= "sequencingread_info";
  public static final String VARIANTCALL_VARIANT_CALLING_TOOL								= "variantcall_variant_calling_tool";
  public static final String VARIANTCALL_MATCHED_NORMAL_SAMPLE_SUBMITTER_ID	= "variantcall_matched_normal_sample_submitter_id";
  public static final String VARIANTCALL_TUMOUR_SAMPLE_SUBMITTER_ID					= "variantcall_tumour_sample_submitter_id";
  public static final String VARIANTCALL_INFO																= "variantcall_info";
  public static final String ANALYSIS_ID																		= "analysis_id";
  public static final String ANALYSIS_TYPE																	= "analysis_type";
  public static final String ANALYSIS_STATE																	= "analysis_state";
  public static final String ANALYSIS_INFO																	= "analysis_info";
  public static final String STUDY_ID																				= "study_id";

  /**
   * File Entity
   */
  @Id
  @Column(name = FILE_OBJECT_ID)
  private String fileObjectId;

  @Column(name = FILE_ACCESS)
  private String fileAccess;

  @Column(name = FILE_MD5)
  private String fileMd5;

  @Column(name = FILE_NAME)
  private String fileName;

  @Column(name = FILE_SIZE)
  private Long fileSize;

  @Column(name = FILE_TYPE)
  private String fileType;

  @Column(name = FILE_INFO)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private Map<String, Object> fileInfo;

  /**
   * Sample Entity
   */
  @Column(name = SAMPLE_ID)
  private String sampleId;

  @Column(name = SAMPLE_SUBMITTER_ID)
  private String sampleSubmitterId;

  @Column(name = SAMPLE_TYPE)
  private String sampleType;

  @Column(name = SAMPLE_INFO)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private Map<String, Object> sampleInfo;

  /**
   * Specimen Entity
   */
  @Column(name = SPECIMEN_ID)
  private String specimenId;

  @Column(name = SPECIMEN_SUBMITTER_ID)
  private String specimenSubmitterId;

  @Column(name = SPECIMEN_TYPE)
  private String specimenType;

  @Column(name = SPECIMEN_CLASS)
  private String specimenClass;

  @Column(name = SPECIMEN_INFO)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private Map<String, Object> specimenInfo;

  /**
   * Donor Entity
   */
  @Column(name = DONOR_ID)
  private String donorId;

  @Column(name = DONOR_SUBMITTER_ID)
  private String donorSubmitterId;

  @Column(name = DONOR_GENDER)
  private String donorGender;

  @Column(name = DONOR_INFO)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private Map<String, Object> donorInfo;

  /**
   * Analysis Entity
   */
  @Column(name = ANALYSIS_ID)
  private String analysisId;

  @Column(name = ANALYSIS_TYPE)
  private String analysisType;

  @Column(name = ANALYSIS_STATE)
  private String analysisState;

  @Column(name = ANALYSIS_INFO)
  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private Map<String, Object> analysisInfo;

  /**
   * Study Entity
   */
  @Column(name = STUDY_ID)
  private String studyId;

}
