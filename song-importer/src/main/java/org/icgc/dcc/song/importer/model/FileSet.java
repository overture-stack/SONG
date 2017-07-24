package org.icgc.dcc.song.importer.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class FileSet {

  @NonNull private final String analysisId;
  @NonNull private final String fileId;

  public static FileSet createFileSet(String analysisId, String fileId) {
    return new FileSet(analysisId, fileId);
  }

}
