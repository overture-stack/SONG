package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;
import static org.springframework.http.ResponseEntity.ok;

import org.icgc.dcc.sodalite.server.model.Upload;
import org.icgc.dcc.sodalite.server.repository.UploadRepository;
import org.icgc.dcc.sodalite.server.validation.SchemaValidator;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class UploadService {

  @Autowired
  IdService id;

  @Autowired
  private SchemaValidator validator;

  protected static final ObjectMapper mapper = new ObjectMapper().registerModule(new ParameterNamesModule())
      .registerModule(new Jdk8Module())
      .registerModule(new JavaTimeModule());

  @Autowired
  private final UploadRepository uploadRepository;

  private void save(@NonNull String studyId, @NonNull String uploadId, @NonNull String jsonPayload) {
    uploadRepository.create(uploadId, studyId, Upload.CREATED, jsonPayload);
  }

  public Upload status(@NonNull String uploadId) {
    return uploadRepository.get(uploadId);
  }

  private void updateAsValid(@NonNull String uploadId) {
    uploadRepository.update(uploadId, Upload.VALIDATED, "");
  }

  private void updateAsInvalid(@NonNull String uploadId, @NonNull String errorMessages) {
    uploadRepository.update(uploadId, Upload.VALIDATION_ERROR, errorMessages);
  }

  private void updateAsPublished(@NonNull String uploadId) {
    uploadRepository.update(uploadId, Upload.PUBLISHED, "");
  }

  public ResponseEntity<String> upload(String schemaName, String studyId, String payload) {
    val uploadId = id.generateUploadId();

    try {
      save(studyId, uploadId, payload);
    } catch (UnableToExecuteStatementException jdbie) {
      log.error(jdbie.getCause().getMessage());
      throw new RepositoryException(jdbie.getCause());
    }

    validate(schemaName, uploadId, payload); // Async operation.

    return ok(uploadId);
  }

  @Async
  private void validate(String schemaId, String uploadId, String payload) {
    try {
      JsonNode jsonNode = mapper.reader().readTree(payload);
      val response = validator.validate(schemaId, jsonNode);

      if (response.isValid()) {
        updateAsValid(uploadId);
      } else {
        updateAsInvalid(uploadId, response.getValidationErrors());
      }
    } catch (JsonProcessingException jpe) {
      log.error(jpe.getMessage());
      updateAsInvalid(uploadId, format("Invalid JSON document submitted: %s", jpe.getMessage()));
    } catch (Exception e) {
      log.error(e.getMessage());
      updateAsInvalid(uploadId, format("Unknown processing problem: %s", e.getMessage()));
    }
  }

  public ResponseEntity<String> publish(@NonNull String uploadId) {
    val s = status(uploadId);
    if (s == null) {
      return status(HttpStatus.NOT_FOUND, "UploadId %s does not exist", uploadId);
    }
    val state = s.getState();
    if (!state.equals(Upload.VALIDATED)) {
      return status(HttpStatus.CONFLICT,
          "UploadId %s is in state '%s', but must be in state 'VALIDATED' before it can be published.", uploadId,
          state);
    }

    updateAsPublished(uploadId);
    // TODO: Create the analysis object here.
    return ok("Successfully published " + uploadId);
  }

  private ResponseEntity<String> status(HttpStatus status, String format, Object... args) {
    return ResponseEntity.status(status).body(format(format, args));
  }

}
