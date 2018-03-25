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

package org.icgc.dcc.song.importer.parser;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class FieldNames {

  public static final String OBJECT_ID = "objectId";
  public static final String PROJECT_CODE = "projectCode";
  public static final String DONOR_ID = "donorId";
  public static final String DONORS = "donors";
  public static final String SAMPLE_ID = "sampleId";
  public static final String SPECIMEN_ID = "specimenId";
  public static final String SPECIMEN_TYPE = "specimenType";
  public static final String ID = "id";
  public static final String DATA_TYPE = "dataType";
  public static final String DATA_CATEGORIZATION = "dataCategorization";
  public static final String LAST_MODIFIED = "lastModified";
  public static final String REFERENCE_GENOME = "referenceGenome";
  public static final String REFERENCE_NAME = "referenceName";
  public static final String EXPERIMENTAL_STRATEGY= "experimentalStrategy";
  public static final String GENOME_BUILD = "genomeBuild";
  public static final String FILE_NAME = "fileName";
  public static final String FILE_COPIES = "fileCopies";
  public static final String FILE_SIZE = "fileSize";
  public static final String FILE_FORMAT = "fileFormat";
  public static final String FILE_MD5SUM = "fileMd5sum";

  public static final String INDEX_FILE_NAME = "indexFileName";
  public static final String INDEX_FILE_SIZE = "indexFileSize";
  public static final String INDEX_FILE_FORMAT = "indexFileFormat";
  public static final String INDEX_FILE_MD5SUM = "indexFileMd5sum";
  public static final String INDEX_FILE_ID= "indexFileId";
  public static final String INDEX_FILE_OBJECT_ID = "indexObjectId";
  public static final String INDEX_FILE_TYPE= "indexFileType";

  public static final String OTHER_IDENTIFIERS = "otherIdentifiers";
  public static final String TCGA_SAMPLE_BARCODE = "tcgaSampleBarcode";
  public static final String TCGA_ALIQUOT_BARCODE= "tcgaAliquotBarcode";
  public static final String ACCESS = "access";
  public static final String DATA_BUNDLE_ID = "dataBundleId";
  public static final String DATA_BUNDLE= "dataBundle";
  public static final String INDEX_FILE = "indexFile";
  public static final String ANALYSIS_METHOD = "analysisMethod";
  public static final String SOFTWARE = "software";
  public static final String PROJECT_ID = "projectId";
  public static final String PROJECT_NAME = "projectName";
  public static final String GENDER = "gender";
  public static final String SUBMITTED_DONOR_ID = "submittedDonorId";
  public static final String SUBMITTED_SAMPLE_ID = "submittedSampleId";
  public static final String SUBMITTED_SPECIMEN_ID = "submittedSpecimenId";
  public static final String SPECIMEN = "specimen";
  public static final String SUBMITTED_ID = "submittedId";
  public static final String TYPE = "type";
  public static final String SAMPLES = "samples";
  public static final String ANALYZED_ID = "analyzedId";
  public static final String STUDY = "study";
  public static final String AVAILABLE_RAW_SEQUENCE_DATA = "availableRawSequenceData";
  public static final String LIBRARY_STRATEGY= "libraryStrategy";
  public static final String REPO_DATA_BUNDLE_ID = "repoDataBundleId";
  public static final String REPO_METADATA_PATH = "repoMetadataPath";
  public static final String REPO_NAME = "repoName";
}
