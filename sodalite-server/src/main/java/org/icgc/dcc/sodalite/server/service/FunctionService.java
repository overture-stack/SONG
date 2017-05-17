package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.SubmissionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
public class FunctionService {

  @Autowired
  StatusService statusService;

  public boolean notifyUpload(String studyId, String uploadId) {
    if (notInState(SubmissionStatus.VALIDATED, studyId, uploadId)) {
      return false;
    }

    statusService.updateAsUploaded(studyId, uploadId);
    return true;
  }

  public int publish(String studyId) {
    return statusService.publishAll(studyId);
  }

  public boolean publishId(String studyId, String uploadId) {
    if (notInState(SubmissionStatus.UPLOADED, studyId, uploadId)) {
      return false;
    }

    statusService.updateAsPublished(studyId, uploadId);
    return true;
  }

  public boolean notInState(String state, String studyId, String uploadId) {
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
