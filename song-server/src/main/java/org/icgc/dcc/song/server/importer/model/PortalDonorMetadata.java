package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.Optional;

@Value
@Builder
public class PortalDonorMetadata implements Serializable{

  @NonNull private final String donorId;
  @NonNull private final String projectId;
  @NonNull private final String submittedDonorId;
  private final String projectName;
  private final String gender;
  @NonNull private final String normalSpecimenId; //Assume that every donor has one donor
  @NonNull private final String normalSubmittedSpecimenId;
  @NonNull private final String normalSpecimenType;
  @NonNull private final String normalSampleId; //Assume theres only one sample
  @NonNull private final String normalAnalyzedId; //Assume theres only one sample

  public Optional<String> getGender() {
    return Optional.of(gender);
  }

  public Optional<String> getProjectName(){
    return Optional.of(projectName);
  }

}
