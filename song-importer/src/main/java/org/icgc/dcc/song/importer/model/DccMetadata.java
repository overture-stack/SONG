package org.icgc.dcc.song.importer.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.icgc.dcc.song.server.model.enums.AccessTypes;

import java.io.Serializable;
import java.util.Optional;

@Value
@RequiredArgsConstructor
public class DccMetadata implements Serializable{

  @NonNull private final String id;
  @NonNull private final AccessTypes accessType;
  private final long createdTime;
  @NonNull private final String fileName;
  @NonNull private final String gnosId;
  private String projectCode;

  public Optional<String> getProjectCode() {
    return Optional.ofNullable(projectCode);
  }

  public static DccMetadata createDccMetadata(String id, AccessTypes accessType, long createdTime,
      String fileName,
      String gnosId, String projectCode) {
    return new DccMetadata(id, accessType, createdTime, fileName, gnosId, projectCode);
  }

}
