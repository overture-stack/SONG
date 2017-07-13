package org.icgc.dcc.song.server.importer.model;

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
  @NonNull private final String dataType;
  @NonNull private final String donorId;
  @NonNull private final String experimentalStrategy;
  @NonNull private final String fileFormat;
  @NonNull private final String fileId;
  @NonNull private final long fileLastModified;
  @NonNull private final String fileMd5sum;
  @NonNull private final String fileName;
  @NonNull private final long fileSize;
  @NonNull private final String genomeBuild;
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
  @NonNull private final String software;

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


}
