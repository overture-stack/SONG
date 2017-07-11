package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Value
@Builder
public class PortalDonorMetadata implements Serializable {

  public static final long serialVersionUID = 1499437054L;

  @NonNull private final String id;
  @NonNull private final String projectId;
  private final String projectName;
  private final String gender;
  private final String submitterDonorId;
  @NonNull @Singular private final List<PortalSpecimenMetadata> specimens;

  public Optional<PortalSpecimenMetadata> getSpecimen(String specimenId){
    return specimens.stream()
        .filter(x -> x.getId().equals(specimenId))
        .findFirst();
  }

  public Optional<String> getGender(){
    return Optional.of(gender);
  }

  public Optional<String> getProjectName() {
    return Optional.of(projectName);
  }

  public Optional<String> getSubmitterDonorId() {
    return Optional.of(submitterDonorId);
  }
}
