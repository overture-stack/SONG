package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PortalDonorMetadata {

  @NonNull private final String id;
  @NonNull private final String gender;
  @NonNull private final String submitterDonorId;
  @NonNull private final List<PortalSpecimenData> specimens;

}
