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
public class TableAttributeNames {
  public static final String ID = "id";
  public static final String STUDY_ID = "study_id";
  public static final String TYPE = "type";
  public static final String STATE = "state";
  public static final String ACCESS = "access";
  public static final String INFO = "info";
  public static final String ID_TYPE = "id_type";

  public static final String SUBMITTER_ID = "submitter_id";
  public static final String GENDER = "gender";

  public static final String MD5 = "md5";
  public static final String SIZE = "size";
  public static final String NAME = "name";
  public static final String OBJECT_ID = "object_id";

  public static final String SPECIMEN_ID = "specimen_id";

  public static final String ALIGNED = "aligned";
  public static final String ALIGNMENT_TOOL = "alignment_tool";
  public static final String INSERT_SIZE = "insert_size";
  public static final String LIBRARY_STRATEGY = "library_strategy";
  public static final String PAIRED_END = "paired_end";
  public static final String REFERENCE_GENOME = "reference_genome";

  public static final String DONOR_ID = "donor_id";
  public static final String CLASS = "class";

  public static final String ANALYSIS_ID = "analysis_id";
  public static final String ANALYSIS_STATE = "analysis_state";

  public static final String ORGANIZATION = "organization";
  public static final String DESCRIPTION = "description";

  public static final String ERRORS = "errors";
  public static final String PAYLOAD = "payload";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";
  public static final String SAMPLE_ID = "sample_id";

  public static final String VARIANT_CALLING_TOOL = "variant_calling_tool";
  public static final String MATCHED_NORMAL_SAMPLE_SUBMITTER_ID =
      "matched_normal_sample_submitter_id";
  public static final String SCHEMA = "schema";
  public static final String VERSION = "version";
  public static final String ANALYSIS_SCHEMA_ID = "analysis_schema_id";
  public static final String DATA = "data";
  public static final String ANALYSIS_DATA_ID = "analysis_data_id";
}
