package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.SubmissionStatus;
import org.icgc.dcc.sodalite.server.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Service
public class StatusService {

  @Autowired
  private final StatusRepository statusRepository;

  public boolean exists(@NonNull String studyId, @NonNull String uploadId) {
    return !statusRepository.checkIfExists(uploadId, studyId).isEmpty();
  }

  public void log(@NonNull String studyId, @NonNull String uploadId, @NonNull String jsonPayload) {
    statusRepository.create(uploadId, studyId, SubmissionStatus.CREATED, jsonPayload);
  }

  public SubmissionStatus getStatus(@NonNull String studyId, @NonNull String uploadId) {
    return statusRepository.get(uploadId, studyId);
  }

  public SubmissionStatus getStatus(@NonNull String uploadId) {
    return statusRepository.get(uploadId);
  }

  public void updateAsValid(@NonNull String studyId, @NonNull String uploadId) {
    statusRepository.update(uploadId, studyId, SubmissionStatus.VALIDATED, "");
  }

  public void updateAsInvalid(@NonNull String studyId, @NonNull String uploadId, @NonNull String errorMessages) {
    statusRepository.update(uploadId, studyId, SubmissionStatus.VALIDATION_ERROR, errorMessages);
  }

  public void updateAsUploaded(@NonNull String studyId, @NonNull String uploadId) {
    statusRepository.update(uploadId, studyId, SubmissionStatus.UPLOADED, "");
  }

  public void updateAsPublished(@NonNull String studyId, @NonNull String uploadId) {
    statusRepository.update(uploadId, studyId, SubmissionStatus.PUBLISHED, "");
  }

  public int publishAll(@NonNull String studyId) {
    return statusRepository.updateState(studyId, SubmissionStatus.UPLOADED, SubmissionStatus.PUBLISHED);
  }

}
