package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.AnalysisState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
public class FunctionService {

  @Autowired
  StatusService statusService;

  public boolean notifyUpload(@NonNull String studyId, @NonNull String uploadId, @NonNull String accessToken) {
    if (notInState(AnalysisState.VALIDATED, studyId, uploadId)) {
      return false;
    }

    statusService.updateAsReceived(studyId, uploadId, accessToken);
    return true;
  }

  public int publish(@NonNull String studyId) {
    return statusService.publishAll(studyId);
  }

  public boolean publishId(@NonNull String studyId, @NonNull String uploadId, @NonNull String accessToken) {
    if (notInState(AnalysisState.READY_FOR_PUBLISH, studyId, uploadId)) {
      return false;
    }

    statusService.updateAsPublished(studyId, uploadId, accessToken);
    return true;
  }

  public boolean notInState(AnalysisState state, String studyId, String uploadId) {
    val s = statusService.getStatus(studyId, uploadId);

    if (s == null) {
      return true;
    }

    if (s.getState().equals(state)) {
      return false;
    }

    return true;
  }

}
