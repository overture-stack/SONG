package bio.overture.song.server.utils.generator;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LegacyAnalysisTypeName {
  VARIANT_CALL("variantCall"),
  SEQUENCING_READ("sequencingRead");

  @NonNull private final String analysisTypeName;

  public static LegacyAnalysisTypeName resolveLegacyAnalysisTypeName(
      @NonNull String analysisTypeName) {
    return stream(values())
        .filter(x -> x.getAnalysisTypeName().equals(analysisTypeName))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    format("The analysis type name '%s' cannot be resolved", analysisTypeName)));
  }
}
