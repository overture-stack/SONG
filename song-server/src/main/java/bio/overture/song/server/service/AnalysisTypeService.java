/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_ANALYSIS_TYPE_NAME;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_JSON_SCHEMA;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.CollectionUtils.isCollectionBlank;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.Separators.COMMA;
import static bio.overture.song.server.controller.analysisType.AnalysisTypeController.REGISTRATION;
import static bio.overture.song.server.repository.specification.AnalysisSchemaSpecification.buildListQuery;
import static bio.overture.song.server.utils.JsonSchemas.PROPERTIES;
import static bio.overture.song.server.utils.JsonSchemas.REQUIRED;
import static bio.overture.song.server.utils.JsonSchemas.buildSchema;
import static bio.overture.song.server.utils.JsonSchemas.validateWithSchema;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.isNull;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang.StringUtils.isBlank;

import bio.overture.song.core.model.AnalysisType;
import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.PageDTO;
import bio.overture.song.server.controller.analysisType.AnalysisTypeController;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

  /** Dependencies */
  private final Schema analysisTypeRegistrationSchema;

  private final AnalysisSchemaRepository analysisSchemaRepository;
  private final String analysisBaseContent;

  @Autowired
  public AnalysisTypeService(
      @NonNull Supplier<Schema> analysisTypeRegistrationSchemaSupplier,
      @Qualifier("analysisBaseJson") @NonNull String analysisBaseContent,
      @NonNull AnalysisSchemaRepository analysisSchemaRepository) {
    this.analysisTypeRegistrationSchema = analysisTypeRegistrationSchemaSupplier.get();
    this.analysisSchemaRepository = analysisSchemaRepository;
    this.analysisBaseContent = analysisBaseContent;
  }

  public Schema getAnalysisTypeRegistrationSchema() {
    return analysisTypeRegistrationSchema;
  }

  public AnalysisType getAnalysisType(
      @NonNull String name, @Nullable Integer version, boolean unrenderedOnly) {
    val resolvedVersion = isNull(version) ? getLatestVersionNumber(name) : version;
    val analysisSchema = getAnalysisSchema(name, resolvedVersion);
    val resolvedSchemaJson =
        resolveSchemaJsonView(analysisSchema.getSchema(), unrenderedOnly, false);
    return AnalysisType.builder()
        .name(name)
        .version(resolvedVersion)
        .schema(resolvedSchemaJson)
        .build();
  }

  @SneakyThrows
  public AnalysisSchema getAnalysisSchema(@NonNull AnalysisTypeId analysisTypeId) {
    val name = analysisTypeId.getName();
    val version = analysisTypeId.getVersion();
    return getAnalysisSchema(name, version);
  }

  @SneakyThrows
  public AnalysisSchema getAnalysisSchema(String name, Integer version) {
    checkServer(!isBlank(name), getClass(), MALFORMED_PARAMETER, "The name parameter is blank");
    validateAnalysisTypeName(name);

    val resolvedVersion = resolveVersion(name, version);
    val result = analysisSchemaRepository.findByNameAndVersion(name, resolvedVersion);
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
          version,
          name,
          latestVersion);
    }
    log.debug("Found analysisType '{}' with version '{}'", name, version);
    return result.get();
  }

  @SneakyThrows
  public AnalysisType getAnalysisType(
      @NonNull AnalysisTypeId analysisTypeId, boolean unrenderedOnly) {
    val analysisSchema = getAnalysisSchema(analysisTypeId);
    val resolvedSchemaJson =
        resolveSchemaJsonView(analysisSchema.getSchema(), unrenderedOnly, false);
    return AnalysisType.builder()
        .name(analysisTypeId.getName())
        .version(analysisTypeId.getVersion())
        .schema(resolvedSchemaJson)
        .build();
  }

  @Transactional
  public AnalysisType register(@NonNull String analysisTypeName, JsonNode analysisTypeSchema) {
    validateAnalysisTypeName(analysisTypeName);
    validateAnalysisTypeSchema(analysisTypeSchema);
    return commitAnalysisType(analysisTypeName, analysisTypeSchema);
  }

  public PageDTO<AnalysisType> listAnalysisTypes(
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
    return convertToPageDTO(new PageImpl<>(analysisTypes, pageable, page.getTotalElements()));
  }

  @SneakyThrows
  public JsonNode resolveSchemaJsonView(
      @NonNull JsonNode unrenderedSchema, boolean unrenderedOnly, boolean hideSchema) {
    return hideSchema
        ? null
        : unrenderedOnly ? unrenderedSchema : renderPayloadJsonSchema(unrenderedSchema);
  }

  public Integer getLatestVersionNumber(String name) {
    validateAnalysisTypeName(name);
    val version = analysisSchemaRepository.countAllByName(name);
    checkServer(
        version > 0,
        getClass(),
        ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' was not found",
        name);
    return version;
  }

  private int resolveVersion(@NonNull String name, Integer nullableVersion) {
    if (isNull(nullableVersion)) {
      return getLatestVersionNumber(name);
    }
    checkServer(
        nullableVersion > 0,
        getClass(),
        MALFORMED_PARAMETER,
        "The version '%s' must be greater than 0",
        nullableVersion);
    return nullableVersion;
  }

  private JsonNode renderPayloadJsonSchema(JsonNode schema) throws IOException {
    val rendered = (ObjectNode) readTree(analysisBaseContent);
    val baseProperties = (ObjectNode) rendered.path(PROPERTIES);
    val schemaProperties = (ObjectNode) schema.path(PROPERTIES);
    if (schema.has(REQUIRED)) {
      checkState(rendered.has(REQUIRED), "The base payload schema should have a required field");
      val baseRequired = (ArrayNode) rendered.path(REQUIRED);
      val schemaRequired = (ArrayNode) schema.path(REQUIRED);
      baseRequired.addAll(schemaRequired);
    }
    baseProperties.setAll(schemaProperties);
    return rendered;
  }

  @SneakyThrows
  private void validateAnalysisTypeSchema(JsonNode analysisTypeSchema) {
    checkServer(
        !isNull(analysisTypeSchema), getClass(), SCHEMA_VIOLATION, "Schema field cannot be null");
    val metaSchema = getAnalysisTypeRegistrationSchema();
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

    val resolvedAnalysisSchema = analysisSchemaRepository.findByNameAndVersion(analysisTypeName, version);
    val createdAt = resolvedAnalysisSchema.isPresent() ? resolvedAnalysisSchema.get().getCreatedAt() : null;

    return AnalysisType.builder()
        .name(analysisTypeName)
        .version(version)
        .createdAt(createdAt)
        .schema(resolvedSchemaJson)
        .build();
  }

  public static AnalysisTypeId resolveAnalysisTypeId(
      @NonNull String name, @NonNull Integer version) {
    return AnalysisTypeId.builder().name(name).version(version).build();
  }

  public static AnalysisTypeId resolveAnalysisTypeId(@NonNull AnalysisSchema analysisSchema) {
    return resolveAnalysisTypeId(analysisSchema.getName(), analysisSchema.getVersion());
  }

  public static AnalysisTypeId resolveAnalysisTypeId(@NonNull AnalysisType analysisType) {
    return resolveAnalysisTypeId(analysisType.getName(), analysisType.getVersion());
  }

  private AnalysisType convertToAnalysisType(
      AnalysisSchema analysisSchema, boolean hideSchema, boolean unrenderedOnly) {
    return AnalysisType.builder()
        .name(analysisSchema.getName())
        .version(analysisSchema.getVersion())
        .createdAt(analysisSchema.getCreatedAt())
        .schema(resolveSchemaJsonView(analysisSchema.getSchema(), unrenderedOnly, hideSchema))
        .build();
  }

  private void validateAnalysisTypeName(@NonNull String analysisTypeName) {
    checkServer(
        ANALYSIS_TYPE_NAME_PATTERN.matcher(analysisTypeName).matches(),
        getClass(),
        MALFORMED_PARAMETER,
        "The analysisTypeId name '%s' does not match the regex: %s",
        analysisTypeName,
        ANALYSIS_TYPE_NAME_PATTERN.pattern());
    checkServer(
        !analysisTypeName.equals(REGISTRATION),
        getClass(),
        ILLEGAL_ANALYSIS_TYPE_NAME,
        "Cannot register an analysisType with name '%s'",
        REGISTRATION);
  }

  private static void validatePositiveVersionsIfDefined(@Nullable Collection<Integer> versions) {
    checkServer(
        isCollectionBlank(versions) || versions.stream().noneMatch(x -> x < 1),
        AnalysisTypeController.class,
        MALFORMED_PARAMETER,
        "The requested versions must be greater than 0");
  }

  private static <T> PageDTO<T> convertToPageDTO(@NonNull Page<T> page) {
    return new PageDTO<>(
        page.getSize(), page.getNumber(), page.getTotalElements(), page.getContent());
  }
}
