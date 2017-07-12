package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.Optional;

@Value
@Builder
public class PortalSampleMetadata implements Serializable{

  public static final long serialVersionUID = 1499437058L;

  @NonNull private final String id;
  @NonNull private final String analyzedId;
  @NonNull private final String study;
  private final String libraryStrategy;

  public Optional<String> getLibraryStrategy(){
    return Optional.of(libraryStrategy);
  }


}
