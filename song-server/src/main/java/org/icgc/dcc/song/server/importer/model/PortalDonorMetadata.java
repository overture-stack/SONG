package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
@Builder
public class PortalDonorMetadata {

  @NonNull private final String id;
  @NonNull private final String projectId;
  @NonNull private final String projectName;
  @NonNull private final String gender;
  @NonNull private final String submitterDonorId;
  @NonNull @Singular private final List<PortalSpecimenMetadata> specimens;

  public Optional<PortalSpecimenMetadata> getSpecimen(String specimenId){
    return specimens.stream()
        .filter(x -> x.getId().equals(specimenId))
        .findFirst();
  }

}
