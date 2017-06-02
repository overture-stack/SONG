package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;
import static org.springframework.http.ResponseEntity.ok;

import org.icgc.dcc.sodalite.server.model.Upload;
import org.icgc.dcc.sodalite.server.model.enums.IdPrefix;
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
  private final IdService id;
  @Autowired
  private final ValidationService validator;
  @Autowired
  private final AnalysisService analysis;

  @Autowired
  private final UploadRepository uploadRepository;

  public Upload read(@NonNull String uploadId) {
    return uploadRepository.get(uploadId);
  }

  private void create(@NonNull String studyId, @NonNull String uploadId, @NonNull String jsonPayload) {
    uploadRepository.create(uploadId, studyId, Upload.CREATED, jsonPayload);
  }

  public ResponseEntity<String> upload(String studyId, String payload) {
    val uploadId = id.generate(IdPrefix.Upload);

    try {
      create(studyId, uploadId, payload);
    } catch (UnableToExecuteStatementException jdbie) {
      log.error(jdbie.getCause().getMessage());
      throw new RepositoryException(jdbie.getCause());
    }

    val analysisType = analysis.getAnalysisType(payload);
    validator.validate(uploadId, payload, analysisType); // Async operation.

    return ok(uploadId);
  }

  public ResponseEntity<String> publish(@NonNull String studyId, @NonNull String uploadId) {
    val s = read(uploadId);
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
    return ok(analysis.create(studyId, json));
  }

  private void updateAsPublished(@NonNull String uploadId) {
    uploadRepository.update(uploadId, Upload.PUBLISHED, "");
  }

  private ResponseEntity<String> status(HttpStatus status, String format, Object... args) {
    return ResponseEntity.status(status).body(format(format, args));
  }

}
