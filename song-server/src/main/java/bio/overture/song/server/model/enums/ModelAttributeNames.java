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

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ModelAttributeNames {

  public static final String STUDY_ID = "studyId";
  public static final String OBJECT_ID = "objectId";
  public static final String GENDER = "gender";
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
  public static final String ID = "id";
  public static final String VERSION = "version";
  public static final String NAME = "name";
  public static final String ANALYSIS_TYPE = "analysisType";
  public static final String ANALYSIS_SCHEMA = "analysisSchema";
  public static final String ANALYSIS_DATA = "analysisData";
  public static final String ANALYSIS_STATE = "analysisState";
  public static final String ANALYSIS_STATE_HISTORY = "analysisStateHistory";
  public static final String SORT = "sort";
  public static final String SORTORDER = "sortOrder";
  public static final String OFFSET = "offset";
  public static final String LIMIT = "limit";
  public static final String FILES = "files";
  public static final String ANALYSIS = "analysis";
}
