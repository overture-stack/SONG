package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PortalSampleData {

  @NonNull private final String id;
  @NonNull private final String analyzedId;
  @NonNull private final String study;
  private final String libraryStrategy;

}
