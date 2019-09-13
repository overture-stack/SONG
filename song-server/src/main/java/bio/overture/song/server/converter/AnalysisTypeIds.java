package bio.overture.song.server.converter;

import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.server.config.ConverterConfig.ANALYSIS_TYPE_NAME_PATTERN;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = PRIVATE)
public final class AnalysisTypeIds {

  public static void checkAnalysisTypeIdName(@NonNull String name) {
    checkServer(
        ANALYSIS_TYPE_NAME_PATTERN.matcher(name).matches(),
        AnalysisTypeIds.class,
        MALFORMED_PARAMETER,
        "The analysisTypeId name '%s' does not match the regex: %s",
        name,
        ANALYSIS_TYPE_NAME_PATTERN.pattern());
  }
}
