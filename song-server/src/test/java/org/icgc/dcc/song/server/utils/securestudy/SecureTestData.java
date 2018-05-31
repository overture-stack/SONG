package org.icgc.dcc.song.server.utils.securestudy;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SecureTestData {
  @NonNull private final String existingStudyId;
  @NonNull private final String nonExistingStudyId;
  @NonNull private final String unrelatedExistingStudyId;
  @NonNull private final String existingId;
  @NonNull private final String nonExistingId;
}
