package bio.overture.song.server.model.analysis;

import static lombok.AccessLevel.PRIVATE;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class AnalysisTypeId {

  @NonNull private final String name;
  @NonNull private final Integer version;

  public static AnalysisTypeId createAnalysisTypeId(String name, int version) {
    return new AnalysisTypeId(name, version);
  }
}
