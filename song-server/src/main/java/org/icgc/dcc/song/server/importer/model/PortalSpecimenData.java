package org.icgc.dcc.song.server.importer.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PortalSpecimenData {

  @NonNull private final String id;
  @NonNull private final String submittedId;
  @NonNull private final String type;
  @NonNull private final List<PortalSampleData> samples;

}
