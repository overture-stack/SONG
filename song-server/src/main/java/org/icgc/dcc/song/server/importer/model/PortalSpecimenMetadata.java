package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
@Builder
public class PortalSpecimenMetadata {

  @NonNull private final String id;
  @NonNull private final String submittedId;
  @NonNull private final String type;
  @NonNull @Singular private final List<PortalSampleMetadata> samples;

  public Optional<PortalSampleMetadata> getSample(String sampleId){
    return samples.stream()
        .filter(x -> x.getId().equals(sampleId))
        .findFirst();
  }

}
