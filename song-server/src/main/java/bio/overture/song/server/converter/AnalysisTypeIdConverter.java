package bio.overture.song.server.converter;

import bio.overture.song.server.config.ConverterConfig;
import bio.overture.song.server.model.analysis.AnalysisTypeId;
import bio.overture.song.server.model.entity.AnalysisSchema;
import lombok.NonNull;
import lombok.val;
import org.mapstruct.Mapper;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isBlank;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.server.config.ConverterConfig.ANALYSIS_TYPE_ID_PATTERN;
import static bio.overture.song.server.config.ConverterConfig.ANALYSIS_TYPE_ID_STRING_FORMAT;

/**
 * Responsible for converting TO and FROM objects of type AnalysisTypeId
 */
@Mapper(config = ConverterConfig.class)
public abstract class AnalysisTypeIdConverter {

  public abstract AnalysisTypeId convertToAnalysisTypeId(AnalysisSchema a);

  public String convertToString(AnalysisSchema a) {
    return convertToString(convertToAnalysisTypeId(a));
  }

  public String convertToString(@NonNull AnalysisTypeId a) {
    return a.getName()
        .map(n -> a.getVersion().map(v -> resolveStringFormat(n, v)).orElse(n))
        .orElseThrow(
            () ->
                buildServerException(
                    AnalysisTypeIdConverter.class,
                    MALFORMED_PARAMETER,
                    "The analysisTypeId name must not be null"));
  }

  public AnalysisTypeId convertFromString(String s) {
    checkServer(
        !isBlank(s),
        AnalysisTypeIdConverter.class,
        MALFORMED_PARAMETER,
        "The analysisTypeId must not be blank");
    val matcher = ANALYSIS_TYPE_ID_PATTERN.matcher(s);
    checkServer(matcher.matches(), AnalysisTypeIdConverter.class, MALFORMED_PARAMETER,
        "The analysisTypeId '%s' does not match required regex: '%s'",
        s, ANALYSIS_TYPE_ID_PATTERN.pattern());
    val name = matcher.group(1);
    val version = parseInt(matcher.group(2));
    return AnalysisTypeId.builder().name(name).version(version).build();
  }

  private static String resolveStringFormat(@NonNull String name, @NonNull Integer version) {
    return format(ANALYSIS_TYPE_ID_STRING_FORMAT, name, version);
  }
}
