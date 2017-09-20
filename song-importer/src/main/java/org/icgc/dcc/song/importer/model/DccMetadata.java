package org.icgc.dcc.song.importer.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.icgc.dcc.song.importer.resolvers.AccessTypes;

import java.util.Optional;

@Value
@RequiredArgsConstructor
public class DccMetadata {

  @NonNull private final String cls;
  @NonNull private final String id;
  @NonNull private final AccessTypes accessType;
  private final long createdTime;
  @NonNull private final String fileName;
  @NonNull private final String gnosId;
  private String projectCode;

  public Optional<String> getProjectCode() {
    return Optional.ofNullable(projectCode);
  }

  public static DccMetadata createDccMetadata(String cls, String id, AccessTypes accessType, long createdTime,
      String fileName,
      String gnosId, String projectCode) {
    return new DccMetadata(cls, id, accessType, createdTime, fileName, gnosId, projectCode);
  }

}
