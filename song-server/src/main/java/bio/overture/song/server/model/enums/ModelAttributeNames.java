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

package bio.overture.song.server.model.enums;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ModelAttributeNames {

  public static final String DONOR_ID = "donorId";
  public static final String DONOR_SUBMITTER_ID = "donorSubmitterId";
  public static final String STUDY_ID = "studyId";
  public static final String STUDY = "study";
  public static final String SPECIMEN_ID = "specimenId";
  public static final String SAMPLE_ID = "sampleId";
  public static final String OBJECT_ID = "objectId";
  public static final String DONOR_GENDER = "donorGender";
  public static final String SPECIMENS = "specimens";
  public static final String INFO = "info";
  public static final String ANALYSIS_ID = "analysisId";
  public static final String ALIGNED = "aligned";
  public static final String ALIGNMENT_TOOL = "alignmentTool";
  public static final String INSERT_SIZE = "insertSize";
  public static final String LIBRARY_STRATEGY = "libraryStrategy";
  public static final String PAIRED_END = "pairedEnd";
  public static final String REFERENCE_GENOME = "referenceGenome";
  public static final String UPLOAD_ID = "uploadId";
  public static final String STATE = "state";
  public static final String CREATED_AT = "createdAt";
  public static final String UPDATED_AT = "updatedAt";
  public static final String ERRORS = "errors";
  public static final String PAYLOAD = "payload";
  public static final String VARIANT_CALLING_TOOL = "variantCallingTool";
  public static final String MATCHED_NORMAL_SAMPLE_SUBMITTER_ID = "matchedNormalSampleSubmitterId";
  public static final String ID = "id";
  public static final String VERSION = "version";
  public static final String NAME = "name";
  public static final String OFFSET = "offset";
  public static final String LIMIT = "limit";
  public static final String ANALYSIS_TYPE = "analysisType";
  public static final String ANALYSIS_SCHEMA = "analysisSchema" ;
  public static final String ANALYSIS_DATA = "analysisData";
  public static final String ANALYSIS = "analysis";
  public static final String ANALYSES = "analyses";
  public static final String ANALYSIS_TYPE_ID = "analysisTypeId";
}
