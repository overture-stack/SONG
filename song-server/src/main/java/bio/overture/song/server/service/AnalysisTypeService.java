package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonSchemaUtils.validateWithSchema;
import static bio.overture.song.server.model.analysis.AnalysisTypeId.createAnalysisTypeId;
import static bio.overture.song.server.repository.specification.AnalysisSchemaSpecification.buildListQuery;
import static bio.overture.song.server.utils.CollectionUtils.isCollectionBlank;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;

import bio.overture.song.server.controller.analysisType.AnalysisTypeController;
import bio.overture.song.server.model.analysis.AnalysisTypeId;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnalysisTypeService {

  private static final String ANALYSIS_TYPE_NAME_REGEX = "[a-zA-Z0-9\\._-]+";
  private static final Pattern ANALYSIS_TYPE_NAME_PATTERN =
      compile("^" + ANALYSIS_TYPE_NAME_REGEX + "$");
  private static final Pattern ANALYSIS_TYPE_ID_PATTERN =
      compile("^(" + ANALYSIS_TYPE_NAME_REGEX + "):(\\d+)$");
  private static final String ANALYSIS_TYPE_ID_FORMAT = "%s:%s";

  private final Schema analysisTypeMetaSchema;
  private final AnalysisSchemaRepository analysisSchemaRepository;

  @Autowired
  public AnalysisTypeService(
      @NonNull Supplier<Schema> analysisTypeMetaSchemaSupplier,
      @NonNull AnalysisSchemaRepository analysisSchemaRepository) {
    this.analysisTypeMetaSchema = analysisTypeMetaSchemaSupplier.get();
    this.analysisSchemaRepository = analysisSchemaRepository;
  }

  public Schema getAnalysisTypeMetaSchema() {
    return analysisTypeMetaSchema;
  }

  public AnalysisType getAnalysisType(@NonNull String analysisTypeIdAsString) {
    // Parse out the name and version
    val analysisTypeId = parseAnalysisTypeId(analysisTypeIdAsString);
    val name = analysisTypeId.getName();
    val version = analysisTypeId.getVersion();

    checkServer(
        version > 0,
        getClass(),
        MALFORMED_PARAMETER,
        "The version '%s' must be greater than 0",
        version);

    val result = analysisSchemaRepository.findByNameAndVersion(name, version);
    if (!result.isPresent()) {
      val latestVersion = getLatestVersionNumber(name);
      checkServer(
          latestVersion > 0,
          getClass(),
          ANALYSIS_TYPE_NOT_FOUND,
          "The analysisType with name '%s' does not exist",
          name);
      throw buildServerException(
          getClass(),
          ANALYSIS_TYPE_NOT_FOUND,
          "Version '%s' of analysisType with name '%s' does not exist however exists for the latest version '%s'",
          name,
          version,
          latestVersion);
    }
    val analysisSchema = result.get();
    log.debug("Found analysisType '{}' with version '{}'", name, version);
    return buildAnalysisType(name, version, analysisSchema.getSchema());
  }

  @Transactional
  public AnalysisType register(
      @NonNull String analysisTypeName, @NonNull JsonNode analysisTypeSchema) {
    validateAnalysisTypeName(analysisTypeName);
    validateAnalysisTypeSchema(analysisTypeSchema);
    return commitAnalysisType(analysisTypeName, analysisTypeSchema);
  }

  public Page<AnalysisType> listAnalysisTypes(
      @Nullable Collection<String> names,
      @Nullable Collection<Integer> versions,
      @NonNull Pageable pageable,
      boolean hideSchema) {
    validatePositiveVersionsIfDefined(versions);
    val spec = buildListQuery(names, versions);
    val page = analysisSchemaRepository.findAll(spec, pageable);
    val analysisTypes =
        page.getContent().stream()
            .map(a -> convertToAnalysisType(a, hideSchema))
            .collect(toImmutableList());
    return new PageImpl<>(analysisTypes, pageable, page.getTotalElements());
  }

  private Integer getLatestVersionNumber(String name) {
    return analysisSchemaRepository.countAllByName(name);
  }

  @SneakyThrows
  private void validateAnalysisTypeSchema(@NonNull JsonNode analysisTypeSchema) {
    val metaSchema = getAnalysisTypeMetaSchema();
    try {
      validateWithSchema(metaSchema, analysisTypeSchema);
    } catch (ValidationException e) {
      throw buildServerException(getClass(), SCHEMA_VIOLATION, COMMA.join(e.getAllMessages()));
    }
  }

  private AnalysisType commitAnalysisType(
      @NonNull String analysisTypeName, @NonNull JsonNode analysisTypeSchema) {
    val analysisSchema =
        AnalysisSchema.builder().name(analysisTypeName).schema(analysisTypeSchema).build();
    analysisSchemaRepository.save(analysisSchema);
    val version =
        analysisSchemaRepository.countAllByNameAndIdLessThanEqual(
            analysisTypeName, analysisSchema.getId());
    analysisSchema.setVersion(version);
    log.debug("Registered analysisType '{}' with version '{}'", analysisTypeName, version);
    return buildAnalysisType(analysisTypeName, version, analysisSchema.getSchema());
  }

  public static AnalysisType buildAnalysisType(@NonNull String name, int version, JsonNode schema) {
    return AnalysisType.builder()
        .id(resolveAnalysisTypeId(name, version))
        .name(name)
        .version(version)
        .schema(schema)
        .build();
  }

  public static String resolveAnalysisTypeId(@NonNull AnalysisTypeId analysisTypeId) {
    return resolveAnalysisTypeId(analysisTypeId.getName(), analysisTypeId.getVersion());
  }

  public static String resolveAnalysisTypeId(@NonNull String name, int version) {
    return format(ANALYSIS_TYPE_ID_FORMAT, name, version);
  }

  public static String resolveAnalysisTypeId(@NonNull AnalysisType analysisType) {
    return resolveAnalysisTypeId(analysisType.getName(), analysisType.getVersion());
  }

  private static AnalysisTypeId parseAnalysisTypeId(@NonNull String id) {
    val matcher = ANALYSIS_TYPE_ID_PATTERN.matcher(id);
    checkServer(
        matcher.matches(),
        AnalysisTypeService.class,
        MALFORMED_PARAMETER,
        "The id '%s' does not match the regex '%s'",
        id,
        ANALYSIS_TYPE_ID_PATTERN.pattern());
    val name = matcher.group(1);
    val version = parseInt(matcher.group(2));
    return createAnalysisTypeId(name, version);
  }

  private static AnalysisType convertToAnalysisType(
      AnalysisSchema analysisSchema, boolean hideSchema) {
    return buildAnalysisType(
        analysisSchema.getName(),
        analysisSchema.getVersion(),
        hideSchema ? null : analysisSchema.getSchema());
  }

  private static void validateAnalysisTypeName(@NonNull String analysisTypeName) {
    checkServer(
        ANALYSIS_TYPE_NAME_PATTERN.matcher(analysisTypeName).matches(),
        AnalysisTypeService.class,
        MALFORMED_PARAMETER,
        "The analysisType name '%s' does not match the regex",
        analysisTypeName,
        ANALYSIS_TYPE_NAME_PATTERN.pattern());
  }

  private static void validatePositiveVersionsIfDefined(@Nullable Collection<Integer> versions) {
    checkServer(
        isCollectionBlank(versions) || versions.stream().noneMatch(x -> x < 1),
        AnalysisTypeController.class,
        MALFORMED_PARAMETER,
        "The requested versions must be greater than 0");
  }
}
