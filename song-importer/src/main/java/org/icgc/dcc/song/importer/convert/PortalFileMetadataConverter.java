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

package org.icgc.dcc.song.importer.convert;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Lombok;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;

import static org.icgc.dcc.song.core.utils.JsonUtils.toPrettyJson;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getAccess;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getDataType;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getDonorId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getExperimentalStrategy;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileFormat;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileLastModified;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileMd5sum;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileName;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getFileSize;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getGenomeBuild;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileFileFormat;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileFileMd5sum;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileFileName;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileFileSize;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getIndexFileObjectId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getObjectId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getProjectCode;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getRepoDataBundleId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getRepoMetadataPath;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSampleIds;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSoftware;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSpecimenIds;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSpecimenTypes;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSubmittedDonorId;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSubmittedSampleIds;
import static org.icgc.dcc.song.importer.parser.FilePortalJsonParser.getSubmittedSpecimenIds;

@Slf4j
@NoArgsConstructor
public class PortalFileMetadataConverter {

  public static PortalFileMetadata convertToPortalFileMetadata(JsonNode o, String repoName){
    try {
      return PortalFileMetadata.builder()
          .access              (getAccess(o))
          .repoDataBundleId    (getRepoDataBundleId(o, repoName))
          .repoMetadataPath    (getRepoMetadataPath(o, repoName))
          .dataType            (getDataType(o))
          .donorId             (getDonorId(o))
          .experimentalStrategy(getExperimentalStrategy(o))
          .fileFormat          (getFileFormat(o))
          .fileId              (getFileId(o))
          .fileLastModified    (getFileLastModified(o))
          .fileMd5sum          (getFileMd5sum(o))
          .fileName            (getFileName(o))
          .fileSize            (getFileSize(o))
          .genomeBuild         (getGenomeBuild(o))
          .indexFileFileFormat (getIndexFileFileFormat(o).orElse(null))
          .indexFileFileMd5sum (getIndexFileFileMd5sum(o).orElse(null))
          .indexFileFileName   (getIndexFileFileName(o).orElse(null))
          .indexFileFileSize   (getIndexFileFileSize(o).orElse(null))
          .indexFileId         (getIndexFileId(o).orElse(null))
          .indexFileObjectId   (getIndexFileObjectId(o).orElse(null))
          .objectId            (getObjectId(o))
          .projectCode         (getProjectCode(o))
          .sampleIds           (getSampleIds(o))
          .specimenIds         (getSpecimenIds(o))
          .specimenTypes        (getSpecimenTypes(o))
          .submittedDonorId    (getSubmittedDonorId(o))
          .submittedSampleIds  (getSubmittedSampleIds(o))
          .submittedSpecimenIds(getSubmittedSpecimenIds(o))
          .software            (getSoftware(o))
          .build();
    } catch (Throwable t){
      log.error("OBJECT_DATA:\n{}", toPrettyJson(o));
      throw Lombok.sneakyThrow(t);
    }
  }


}
