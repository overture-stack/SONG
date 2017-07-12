package org.icgc.dcc.song.server.importer.model;

import lombok.Value;

import java.util.Optional;

@Value
public class PortalDonorMetadata {

  private final String projectName;
  private final String gender;

  public Optional<String> getGender() {
    return Optional.of(gender);
  }

  public Optional<String> getProjectName(){
    return Optional.of(projectName);
  }

  public static PortalDonorMetadata createPortalDonorMetadata(String projectName, String gender) {
    return new PortalDonorMetadata(projectName, gender);
  }

}
