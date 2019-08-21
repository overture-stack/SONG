package bio.overture.song.server.service;

import bio.overture.song.server.controller.analysisType.AnalysisTypeController;
import bio.overture.song.server.model.analysis.AnalysisTypeId;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_JSON_SCHEMA;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.server.model.analysis.AnalysisTypeId.createAnalysisTypeId;
import static bio.overture.song.server.repository.specification.AnalysisSchemaSpecification.buildListQuery;
import static bio.overture.song.server.utils.CollectionUtils.isCollectionBlank;
import static bio.overture.song.server.utils.JsonSchemas.buildSchema;
import static bio.overture.song.server.utils.JsonSchemas.validateWithSchema;

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
  private final String analysisPayloadBaseContent;

  @Autowired
  public AnalysisTypeService(
      @NonNull Supplier<Schema> analysisTypeMetaSchemaSupplier,
      @NonNull String analysisPayloadBaseContent,
      @NonNull AnalysisSchemaRepository analysisSchemaRepository) {
    this.analysisTypeMetaSchema = analysisTypeMetaSchemaSupplier.get();
    this.analysisSchemaRepository = analysisSchemaRepository;
    this.analysisPayloadBaseContent = analysisPayloadBaseContent;
  }

  public Schema getAnalysisTypeMetaSchema() {
    return analysisTypeMetaSchema;
  }

  public AnalysisType getAnalysisType(
      @NonNull String analysisTypeIdAsString, boolean unrenderedOnly) {
    // Parse out the name and version
    val analysisTypeId = parseAnalysisTypeId(analysisTypeIdAsString);
    return getAnalysisType(analysisTypeId, unrenderedOnly);
  }

  @SneakyThrows
  public AnalysisType getAnalysisType(
      @NonNull AnalysisTypeId analysisTypeId, boolean unrenderedOnly) {
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
    log.debug("Found analysisType '{}' with version '{}'", name, version);
    val resolvedSchemaJson = resolveSchemaJsonView(result.get().getSchema(), unrenderedOnly, false);
    return buildAnalysisType(name, version, resolvedSchemaJson);
  }

  // Retrospect:  may have been a better choice to validate the entire schema (including json schema
  // for file, sample, specimen...etc) against the meta schema (which requires a fixed schema for
  // files, specimen, samples...etc) instead of these rules below
  private JsonNode renderPayloadJsonSchema(JsonNode schema) throws IOException {
    val rendered = (ObjectNode) readTree(analysisPayloadBaseContent);
    val baseProperties = (ObjectNode) rendered.path("properties");
    val schemaProperties = (ObjectNode) schema.path("properties");
    if (schema.has("required")) {
      checkState(rendered.has("required"), "The base payload schema should have a required field");
      val baseRequired = (ArrayNode) rendered.path("required");
      val schemaRequired = (ArrayNode) schema.path("required");
      baseRequired.addAll(schemaRequired);
    }
    baseProperties.setAll(schemaProperties);
    return rendered;
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
      boolean hideSchema,
      boolean unrenderedOnly) {
    validatePositiveVersionsIfDefined(versions);
    val spec = buildListQuery(names, versions);
    val page = analysisSchemaRepository.findAll(spec, pageable);
    val analysisTypes =
        page.getContent().stream()
            .map(a -> convertToAnalysisType(a, hideSchema, unrenderedOnly))
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
      buildSchema(analysisTypeSchema);
    } catch (ValidationException e) {
      throw buildServerException(getClass(), SCHEMA_VIOLATION, COMMA.join(e.getAllMessages()));
    } catch (SchemaException e) {
      throw buildServerException(getClass(), MALFORMED_JSON_SCHEMA, e.getMessage());
    }
  }

  @SneakyThrows
  public JsonNode resolveSchemaJsonView(
      @NonNull JsonNode unrenderedSchema, boolean unrenderedOnly, boolean hideSchema) {
    return hideSchema
        ? null
        : unrenderedOnly ? unrenderedSchema : renderPayloadJsonSchema(unrenderedSchema);
  }

  @SneakyThrows
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
    val resolvedSchemaJson = resolveSchemaJsonView(analysisSchema.getSchema(), false, false);
    return buildAnalysisType(analysisTypeName, version, resolvedSchemaJson);
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

  public static String resolveAnalysisTypeId(@NonNull String name, Object version) {
    return format(ANALYSIS_TYPE_ID_FORMAT, name, version);
  }

  public static String resolveAnalysisTypeId(@NonNull AnalysisSchema analysisSchema) {
    return resolveAnalysisTypeId(analysisSchema.getName(), analysisSchema.getVersion());
  }

  public static String resolveAnalysisTypeId(@NonNull AnalysisType analysisType) {
    return resolveAnalysisTypeId(analysisType.getName(), analysisType.getVersion());
  }

  public static AnalysisTypeId parseAnalysisTypeId(String id) {
    checkServer(!isBlank(id), AnalysisTypeService.class, MALFORMED_PARAMETER, "The analysisTypeId must not be blank");
    val matcher = ANALYSIS_TYPE_ID_PATTERN.matcher(id);
    checkServer(
        matcher.matches(),
        AnalysisTypeService.class,
        MALFORMED_PARAMETER,
        "The analysisTypeId '%s' does not match the regex '%s'",
        id,
        ANALYSIS_TYPE_ID_PATTERN.pattern());
    val name = matcher.group(1);
    val version = parseInt(matcher.group(2));
    return createAnalysisTypeId(name, version);
  }

  private AnalysisType convertToAnalysisType(
      AnalysisSchema analysisSchema, boolean hideSchema, boolean unrenderedOnly) {
    return buildAnalysisType(
        analysisSchema.getName(),
        analysisSchema.getVersion(),
        resolveSchemaJsonView(analysisSchema.getSchema(), unrenderedOnly, hideSchema));
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
