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

import bio.overture.song.core.model.file.FileData;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.repository.UploadRepository;
import bio.overture.song.server.validation.SchemaValidator;
import bio.overture.song.server.validation.ValidationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

  private static final String STUDY = "study";
  private static final String FILE_DATA_SCHEMA_ID = "fileData";
  private static final String STORAGE_DOWNLOAD_RESPONSE_SCHEMA_ID = "storageDownloadResponse";


  @Autowired
  private SchemaValidator validator;

  @Autowired(required = false)
  private Long validationDelayMs = -1L;

  protected static final ObjectMapper mapper = new ObjectMapper().registerModule(new ParameterNamesModule())
      .registerModule(new Jdk8Module())
      .registerModule(new JavaTimeModule());

  @Autowired
  private final UploadRepository uploadRepository;

  private String upperCaseFirstLetter(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  @Async
  public void asyncValidate(@NonNull String uploadId, @NonNull String payload, String analysisType) {
    syncValidate(uploadId, payload, analysisType);
  }

  public void syncValidate(@NonNull String uploadId, @NonNull String payload, String analysisType) {
    log.info("Validating payload for upload Id=" + uploadId + "payload=" + payload);
    log.info(format("Analysis type='%s'",analysisType));
    val errors = validate(payload, analysisType);
    update(uploadId, errors.orElse(null));
  }

  public Optional<String> validate(@NonNull String payload, @NonNull String analysisType) {
    String errors;

    log.info(format("Analysis type='%s'",analysisType));
    try {
      val jsonNode = JsonUtils.readTree(payload);
      val schemaId = "upload" + upperCaseFirstLetter(analysisType);
      val response = validator.validate(schemaId, jsonNode);
      if (response.isValid()) {
        errors = null;
      } else {
        errors =  response.getValidationErrors();
      }
    } catch (JsonProcessingException jpe) {
      log.error(jpe.getMessage());
      errors =  format("Invalid JSON document submitted: %s", jpe.getMessage());
    } catch (Exception e) {
      log.error(e.getMessage());
      errors =  format("Unknown processing problem: %s", e.getMessage());
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

  public Optional<String> validate(FileData fileData){
    val json = JsonUtils.mapper().valueToTree(fileData);
    val resp = validator.validate(FILE_DATA_SCHEMA_ID, json);
    return processResponse(resp);
  }

  public Optional<String> validateStorageDownloadResponse(JsonNode response){
    return processResponse(validator.validate(STORAGE_DOWNLOAD_RESPONSE_SCHEMA_ID, response));
  }

  private void updateState(@NonNull String uploadId, @NonNull UploadStates state, @NonNull String errors) {
    uploadRepository.findById(uploadId)
        .map(x -> {
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

  private static Optional<String> processResponse(ValidationResponse response){
    if (response.isValid()){
      return Optional.empty();
    }else {
      return Optional.of(response.getValidationErrors());
    }
  }


}
