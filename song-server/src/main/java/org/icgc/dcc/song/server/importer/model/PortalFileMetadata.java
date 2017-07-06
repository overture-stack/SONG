package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PortalFileMetadata {

  @NonNull private final String access;
  @NonNull private final String dataBundleId;
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
  @NonNull private final String indexFileFileFormat;
  @NonNull private final String indexFileFileMd5sum;
  @NonNull private final String indexFileFileName;
  @NonNull private final long indexFileFileSize;
  @NonNull private final String indexFileId;
  @NonNull private final String indexFileObjectId;
  @NonNull private final String objectId;
  @NonNull private final String projectCode;
  @NonNull private final List<String> sampleIds;
  @NonNull private final String software;

}
