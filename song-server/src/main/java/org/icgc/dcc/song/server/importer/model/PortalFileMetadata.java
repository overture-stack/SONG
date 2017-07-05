package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PortalFileMetadata {

  private final String fileId;
  private final String objectId;
  private final String access;
  private final String dataType;
  private final String experimentalStrategy;
  private final String dataBundleId;
  private final String fileName;
  private final long fileSize;
  private final long fileMd5sum;
  private final String fileFormat;
  private final long lastModified;
  private final String indexFileId;
  private final String repoDataBundleId;
  private final String genomeBuild;
  private final String software;

}
