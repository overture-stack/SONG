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
import bio.overture.song.core.model.ExternalValidation;
import bio.overture.song.core.model.FileData;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.repository.UploadRepository;
import bio.overture.song.server.validation.SchemaValidator;
import bio.overture.song.server.validation.ValidationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.View;

@Slf4j
@Service
public class ValidationService {

  private static final String FILE_DATA_SCHEMA_ID = "fileData";
  private static final String STORAGE_DOWNLOAD_RESPONSE_SCHEMA_ID = "storageDownloadResponse";
  private static final String EXTERNAL_URL_TEMPLATE_PATTERN_STUDYID = "\\{study\\}";
  private static final String EXTERNAL_URL_TEMPLATE_PATTERN_DATAVALUE = "\\{value\\}";
  private final SchemaValidator validator;
  private final AnalysisTypeService analysisTypeService;
  private final UploadRepository uploadRepository;
  private final boolean enforceLatest;
  private final Schema analysisTypeIdSchema;
  private final RestTemplate restTemplate;
  private final View error;

  @Autowired
  public ValidationService(
      @Value("${schemas.enforceLatest}") boolean enforceLatest,
      @NonNull SchemaValidator validator,
      @NonNull AnalysisTypeService analysisTypeService,
      @NonNull Supplier<Schema> analysisTypeIdSchemaSupplier,
      @NonNull UploadRepository uploadRepository,
      @NonNull RestTemplate restTemplate,
      View error) {
    this.validator = validator;
    this.analysisTypeService = analysisTypeService;
    this.uploadRepository = uploadRepository;
    this.enforceLatest = enforceLatest;
    this.analysisTypeIdSchema = analysisTypeIdSchemaSupplier.get();
    this.restTemplate = restTemplate;
    this.error = error;
  }

  public Optional<String> validate(@NonNull JsonNode payload, @NonNull String studyId) {
    String errors = null;

    try {
      validateWithSchema(analysisTypeIdSchema, payload);
      val analysisTypeResult = extractAnalysisTypeFromPayload(payload);
      if (analysisTypeResult.isEmpty()) {
        throw new ValidationException("Analysis type not found");
      }
      val analysisTypeId = fromJson(analysisTypeResult.get(), AnalysisTypeId.class);
      val analysisType = analysisTypeService.getAnalysisType(analysisTypeId, true);
      log.debug(
          format(
              "Validation Analysis with schema: name=%s  version=%s",
              analysisType.getName(), analysisType.getVersion()));

      val schema = buildSchema(analysisType.getSchema());
      validateWithSchema(schema, payload);

      val fileTypes = analysisType.getFileTypes();
      if (!fileTypes.isEmpty()) {
        validateFileType(fileTypes, payload);
      }

      val externalValidations = analysisType.getExternalValidations();
      if (!externalValidations.isEmpty()) {
        externalValidations(analysisType.getOptions().getExternalValidations(), payload, studyId);
      }
    } catch (ValidationException e) {
      errors = COMMA.join(e.getAllMessages());
      log.error(errors);
    } catch (JSONException e) {
      errors = e.getMessage();
      log.error(errors);
    }

    return Optional.ofNullable(errors);
  }

  private Optional<String> getValueAtJsonPath(@NonNull JsonNode payload, @NonNull String jsonPath) {
    try {
      String value = JsonPath.read(payload.toString(), "$." + jsonPath);
      return Optional.of(value);
    } catch (Exception e) {
      log.debug(
          String.format("Error reading value for external validation. Reason: %s", e.getMessage()));
      return Optional.empty();
    }
  }

  private String buildExternalUrlFromTemplate(
      @NonNull String urlTemplate, @NonNull String studyId, @NonNull String value) {
    return urlTemplate
        .replaceAll(EXTERNAL_URL_TEMPLATE_PATTERN_STUDYID, studyId)
        .replaceAll(EXTERNAL_URL_TEMPLATE_PATTERN_DATAVALUE, value);
  }

  private void externalValidations(
      List<ExternalValidation> externalValidations, @NonNull JsonNode payload, String studyId)
      throws ValidationException {

    for (ExternalValidation externalValidation : externalValidations) {

      val value = getValueAtJsonPath(payload, externalValidation.getJsonPath());
      if (value.isPresent()) {
        // Only validate vs external source if the value is present in the analysis payload
        val formattedExternalUrl =
            buildExternalUrlFromTemplate(externalValidation.getUrl(), studyId, value.get());
        try {
          val response = restTemplate.getForEntity(formattedExternalUrl, Void.class);
          if (response.getStatusCode().isError()) {
            val errorMessage =
                String.format(
                    "Value '%s' from path '%s' is not permitted as it failed to validate with external validation source.",
                    value.get(), externalValidation.getJsonPath(), formattedExternalUrl);
            log.debug(errorMessage);
            throw new ValidationException(errorMessage);
          }
        } catch (RestClientException e) {
          val errorMessage =
              String.format(
                  "Value '%s' from path '%s' is not permitted as it failed to validate with external validation source.",
                  value.get(), externalValidation.getJsonPath());
          log.info(
              String.format(
                  "Error occurred while executing external validation against url '%s'.",
                  formattedExternalUrl));
          throw new ValidationException(errorMessage);
        }
      }
    }
  }

  private void validateFileType(List<String> fileTypes, @NonNull JsonNode payload)
      throws ValidationException {

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
