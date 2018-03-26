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

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Value
@Builder
public class PortalFileMetadata implements Serializable {

  @NonNull private final String access;
  @NonNull private final String repoDataBundleId;
  @NonNull private final String repoMetadataPath;
  @NonNull private final String dataType;
  @NonNull private final String donorId;
  @NonNull private final String experimentalStrategy;
  @NonNull private final String fileFormat;
  @NonNull private final String fileId;
  private final long fileLastModified;
  @NonNull private final String fileMd5sum;
  @NonNull private final String fileName;
  private final long fileSize;
  private final String genomeBuild;
  private final String indexFileFileFormat;
  private final String indexFileFileMd5sum;
  private final String indexFileFileName;
  private final Long indexFileFileSize;
  private final String indexFileId;
  private final String indexFileObjectId;
  @NonNull private final String objectId;
  @NonNull private final String projectCode;
  @NonNull private final List<String> sampleIds;
  @NonNull private final List<String> specimenIds;
  @NonNull private final List<String> specimenTypes;
  @NonNull private final String submittedDonorId;
  @NonNull private final List<String> submittedSampleIds;
  @NonNull private final List<String> submittedSpecimenIds;
  private final String software;

  public Optional<String> getIndexFileFileFormat(){
    return Optional.ofNullable(indexFileFileFormat);
  }

  public Optional<String> getIndexFileFileMd5sum(){
    return Optional.ofNullable(indexFileFileMd5sum);
  }

  public Optional<String> getIndexFileFileName(){
    return Optional.ofNullable(indexFileFileName);
  }

  public Optional<Long> getIndexFileFileSize(){
    return Optional.ofNullable(indexFileFileSize);
  }

  public Optional<String> getIndexFileId(){
    return Optional.ofNullable(indexFileId);
  }

  public Optional<String> getIndexFileObjectId(){
    return Optional.ofNullable(indexFileObjectId);
  }

  public Optional<String> getSoftware(){
    return Optional.ofNullable(software);
  }

  public Optional<String> getGenomeBuild(){
    return Optional.ofNullable(genomeBuild);
  }

  public boolean isIndexFileComplete(){
    return getIndexFileId().isPresent()
        && getIndexFileFileFormat().isPresent()
        && getIndexFileFileMd5sum().isPresent()
        && getIndexFileFileName().isPresent()
        && getIndexFileFileSize().isPresent()
        && getIndexFileObjectId().isPresent();
  }


}
