package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;
import static org.springframework.http.ResponseEntity.ok;

import org.icgc.dcc.sodalite.server.model.Upload;
import org.icgc.dcc.sodalite.server.repository.UploadRepository;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
  ValidationService validator;
  @Autowired
  AnalysisService analysis;

  @Autowired
  private final UploadRepository uploadRepository;

  public Upload status(@NonNull String uploadId) {
    return uploadRepository.get(uploadId);
  }

  public ResponseEntity<String> upload(String studyId, String payload) {
    val uploadId = id.generateUploadId();

    try {
      save(studyId, uploadId, payload);
    } catch (UnableToExecuteStatementException jdbie) {
      log.error(jdbie.getCause().getMessage());
      throw new RepositoryException(jdbie.getCause());
    }

    validator.validate(uploadId, payload); // Async operation.

    return ok(uploadId);
  }

  public ResponseEntity<String> publish(@NonNull String studyId, @NonNull String uploadId) {
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
    val json = s.getPayload();
    analysis.createAnalysis(studyId, json);
    return ok("Successfully published " + uploadId);
  }

  private void save(@NonNull String studyId, @NonNull String uploadId, @NonNull String jsonPayload) {
    uploadRepository.create(uploadId, studyId, Upload.CREATED, jsonPayload);
  }

  private void updateAsPublished(@NonNull String uploadId) {
    uploadRepository.update(uploadId, Upload.PUBLISHED, "");
  }

  private ResponseEntity<String> status(HttpStatus status, String format, Object... args) {
    return ResponseEntity.status(status).body(format(format, args));
  }

}
