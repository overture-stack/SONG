package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.AnalysisState;
import org.icgc.dcc.sodalite.server.model.SubmissionStatus;
import org.icgc.dcc.sodalite.server.repository.StatusRepository;
import org.skife.jdbi.v2.sqlobject.Bind;
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

  public void log(String studyId, String uploadId, String jsonPayload, final String accessToken) {
    statusRepository.create(uploadId, studyId, AnalysisState.RECEIVED.value(), jsonPayload, accessToken);
  }

  public SubmissionStatus getStatus(@NonNull String studyId, @NonNull String uploadId) {
    val status = statusRepository.get(uploadId, studyId);
    return status;
  }

  public void updateAsValid(String studyId, String uploadId) {
    statusRepository.updateState(uploadId, studyId, AnalysisState.VALIDATED.value(), "", "SYSTEM");
  }

  public void updateAsInvalid(String studyId, String uploadId, String errorMessages) {
    statusRepository.updateState(uploadId, studyId, AnalysisState.ERROR.value(), errorMessages, "SYSTEM");
  }

  public void updateAsReceived(@NonNull String studyId, @NonNull String uploadId, final String accessToken) {
    statusRepository.updateState(uploadId, studyId, AnalysisState.RECEIVED.value(), "", accessToken);
  }

  public void updateAsPublished(@NonNull String studyId, @NonNull String uploadId, final String accessToken) {
    statusRepository.updateState(uploadId, studyId, AnalysisState.PUBLISHED.value(), "", accessToken);
  }

  public int publishAll(@NonNull String studyId) {
    return statusRepository.updateState(studyId, AnalysisState.READY_FOR_PUBLISH.value(), AnalysisState.PUBLISHED.value());
  }

}
