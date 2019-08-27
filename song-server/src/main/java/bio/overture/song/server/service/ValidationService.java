/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
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

import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_TYPE_ID;
import static bio.overture.song.server.service.AnalysisTypeService.parseAnalysisTypeId;
import static bio.overture.song.server.utils.JsonParser.extractAnalysisTypeIdFromPayload;
import static bio.overture.song.server.utils.JsonSchemas.buildSchema;
import static bio.overture.song.server.utils.JsonSchemas.validateWithSchema;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.model.file.FileData;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.repository.UploadRepository;
import bio.overture.song.server.validation.SchemaValidator;
import bio.overture.song.server.validation.ValidationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidationService {

  private static final String FILE_DATA_SCHEMA_ID = "fileData";
  private static final String STORAGE_DOWNLOAD_RESPONSE_SCHEMA_ID = "storageDownloadResponse";

  private final SchemaValidator validator;
  private final AnalysisTypeService analysisTypeService;
  private final UploadRepository uploadRepository;

  @Autowired
  public ValidationService(
      @NonNull SchemaValidator validator,
      @NonNull AnalysisTypeService analysisTypeService,
      @NonNull UploadRepository uploadRepository) {
    this.validator = validator;
    this.analysisTypeService = analysisTypeService;
    this.uploadRepository = uploadRepository;
  }

  @Async
  public void asyncValidate(@NonNull String uploadId, @NonNull JsonNode payload) {
    syncValidate(uploadId, payload);
  }

  public void syncValidate(@NonNull String uploadId, @NonNull JsonNode payload) {
    log.info("Validating payload for upload Id=" + uploadId + "payload=" + payload);
    val errors = validate(payload);
    update(uploadId, errors.orElse(null));
  }

  public Optional<String> validate(@NonNull JsonNode payload) {
    String errors = null;
    try {
      val analysisTypeIdResult = extractAnalysisTypeIdFromPayload(payload);
      if (!analysisTypeIdResult.isPresent()) {
        errors = format("Missing the '%s' field", ANALYSIS_TYPE_ID);
      } else {
        val analysisTypeId = parseAnalysisTypeId(analysisTypeIdResult.get());
        val analysisType = analysisTypeService.getAnalysisType(analysisTypeId, false);
        log.info(
            format(
                "Found Analysis type: name=%s  version=%s",
                analysisType.getName(), analysisType.getVersion()));
        val schema = buildSchema(analysisType.getSchema());
        validateWithSchema(schema, payload);
      }
    } catch (ServerException e) {
      log.error(e.getSongError().toPrettyJson());
      errors = e.getSongError().getMessage();
    } catch (ValidationException e) {
      errors = COMMA.join(e.getAllMessages());
      log.error(errors);
    } catch (Exception e) {
      log.error(e.getMessage());
      errors = format("Unknown processing problem: %s", e.getMessage());
    }
    return Optional.ofNullable(errors);
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
    val json = JsonUtils.mapper().valueToTree(fileData);
    val resp = validator.validate(FILE_DATA_SCHEMA_ID, json);
    return processResponse(resp);
  }

  // TODO: transition to everit json schema library
  public Optional<String> validateStorageDownloadResponse(JsonNode response) {
    return processResponse(validator.validate(STORAGE_DOWNLOAD_RESPONSE_SCHEMA_ID, response));
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

  private void updateAsInvalid(@NonNull String uploadId, @NonNull String errorMessages) {
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
