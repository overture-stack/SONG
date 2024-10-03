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

import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.Separators.COMMA;
import static bio.overture.song.server.utils.JsonParser.extractAnalysisTypeFromPayload;
import static bio.overture.song.server.utils.JsonSchemas.buildSchema;
import static bio.overture.song.server.utils.JsonSchemas.validateWithSchema;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isBlank;

import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.FileData;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.repository.UploadRepository;
import bio.overture.song.server.validation.SchemaValidator;
import bio.overture.song.server.validation.ValidationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidationService {

  private static final String FILE_DATA_SCHEMA_ID = "fileData";
  private static final String STORAGE_DOWNLOAD_RESPONSE_SCHEMA_ID = "storageDownloadResponse";

  private final SchemaValidator validator;
  private final AnalysisTypeService analysisTypeService;
  private final UploadRepository uploadRepository;
  private final boolean enforceLatest;
  private final Schema analysisTypeIdSchema;

  @Autowired
  public ValidationService(
      @Value("${schemas.enforceLatest}") boolean enforceLatest,
      @NonNull SchemaValidator validator,
      @NonNull AnalysisTypeService analysisTypeService,
      @NonNull Supplier<Schema> analysisTypeIdSchemaSupplier,
      @NonNull UploadRepository uploadRepository) {
    this.validator = validator;
    this.analysisTypeService = analysisTypeService;
    this.uploadRepository = uploadRepository;
    this.enforceLatest = enforceLatest;
    this.analysisTypeIdSchema = analysisTypeIdSchemaSupplier.get();
  }

  @SneakyThrows
  public Optional<String> validate(@NonNull JsonNode payload) {
    String errors = null;
    try {
      validateWithSchema(analysisTypeIdSchema, payload);
      val analysisTypeResult = extractAnalysisTypeFromPayload(payload);
      val analysisTypeId = fromJson(analysisTypeResult.get(), AnalysisTypeId.class);
      val analysisType = analysisTypeService.getAnalysisType(analysisTypeId, false);
      log.info(
          format(
              "Found Analysis type: name=%s  version=%s",
              analysisType.getName(), analysisType.getVersion()));

      List<String> fileTypes = new ArrayList<>();

      if (analysisType.getOptions() != null && analysisType.getOptions().getFileTypes() != null) {
        fileTypes = analysisType.getOptions().getFileTypes();
      }
      // Only use the file type rules from the corresponding schema version
      Integer schemaVersion = analysisType.getVersion();
      if (Objects.isNull(schemaVersion)) {
        // No version specified, use the latest schema version
        if (fileTypes.isEmpty()) {
          // Reuse file types from the latest schema version
          List<AnalysisSchema> previousSchemas = analysisTypeService.getAllAnalysisSchemas(analysisType.getName());
          List<String> previousFileTypes = previousSchemas.stream()
              .filter(schema -> schema.getFileTypes() != null)
              .flatMap(schema -> schema.getFileTypes().stream())
              .distinct()
              .collect(Collectors.toList());
          if (!previousFileTypes.isEmpty()) {
            validateFileType(previousFileTypes, payload);
          }
        } else {
          // File types are defined in the current schema
          validateFileType(fileTypes, payload);
        }
      } else {
        // Specific schema version is defined, use its file types
        if (!fileTypes.isEmpty()) {
          validateFileType(fileTypes, payload);
        }
      }
      val schema = buildSchema(analysisType.getSchema());
      validateWithSchema(schema, payload);
    } catch (ValidationException e) {
      errors = COMMA.join(e.getAllMessages());
      log.error(errors);
    }
    return Optional.ofNullable(errors);
  }

  private void validateFileType(List<String> fileTypes, @NonNull JsonNode payload) {

    if (CollectionUtils.isNotEmpty(fileTypes)) {
      JsonNode files = payload.get("files");
      if (files.isArray()) {
        for (JsonNode file : files) {
          log.info("file is " + file);
          String fileType = file.get("fileType").asText();
          String fileName = file.get("fileName").asText();
          if (!fileTypes.contains(fileType)) {
            throw new ValidationException(
                String.format(
                    "%s name is not supported, supported formats are %s",
                    fileName, String.join(", ", fileTypes)));
          }
        }
      }
    }
  }

  public void update(@NonNull String uploadId, String errorMessages) {
    if (isNull(errorMessages)) {
      updateAsValid(uploadId);
    } else {
      updateAsInvalid(uploadId, errorMessages);
    }
  }

  // TODO: transition to everit json schema library
  public Optional<String> validate(FileData fileData) {
    val json = mapper().valueToTree(fileData);
    val resp = validator.validate(FILE_DATA_SCHEMA_ID, json);
    return processResponse(resp);
  }

  // TODO: transition to everit json schema library
  public Optional<String> validateStorageDownloadResponse(JsonNode response) {
    return processResponse(validator.validate(STORAGE_DOWNLOAD_RESPONSE_SCHEMA_ID, response));
  }

  public String validateAnalysisTypeVersion(AnalysisTypeId a) {
    checkServer(
        !isBlank(a.getName()),
        getClass(),
        MALFORMED_PARAMETER,
        "The analysisType name cannot be null");
    return validateAnalysisTypeVersion(a.getName(), a.getVersion());
  }

  public String validateAnalysisTypeVersion(@NonNull String name, Integer version) {
    if (enforceLatest && !isNull(version)) {
      val latestVersion = analysisTypeService.getLatestVersionNumber(name);
      if (!version.equals(latestVersion)) {
        val message =
            format(
                "Must use the latest version '%s' while enforceLatest=true, but using version '%s' of analysisType '%s' instead",
                latestVersion, version, name);
        log.error(message);
        return message;
      }
    }
    return null;
  }

  private void updateState(
      @NonNull String uploadId, @NonNull UploadStates state, @NonNull String errors) {
    uploadRepository
        .findById(uploadId)
        .map(
            x -> {
              x.setState(state);
              x.setErrors(errors);
              return x;
            })
        .ifPresent(uploadRepository::save);
  }

  private void updateAsValid(@NonNull String uploadId) {
    updateState(uploadId, UploadStates.VALIDATED, "");
  }

  public void updateAsInvalid(@NonNull String uploadId, @NonNull String errorMessages) {
    updateState(uploadId, UploadStates.VALIDATION_ERROR, errorMessages);
  }

  private static Optional<String> processResponse(ValidationResponse response) {
    if (response.isValid()) {
      return Optional.empty();
    } else {
      return Optional.of(response.getValidationErrors());
    }
  }
}









