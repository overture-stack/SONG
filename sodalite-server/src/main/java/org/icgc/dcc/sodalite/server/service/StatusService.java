package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.SubmissionStatus;
import org.icgc.dcc.sodalite.server.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatusService {
	
  @Autowired
  private final StatusRepository statusRepository;
	
  public boolean exists(String studyId, String uploadId) {
    return !statusRepository.checkIfExists(uploadId, studyId).isEmpty();
  }
  
  public void log(String studyId, String uploadId, String jsonPayload) {
    statusRepository.create(uploadId, studyId, SubmissionStatus.CREATED, jsonPayload);
  }
  
  public SubmissionStatus getStatus(String studyId, String uploadId) {
    val status = statusRepository.get(uploadId, studyId);
    return status;
  }
  
  public void updateAsValid(String studyId, String uploadId) {
    statusRepository.update(uploadId, studyId, SubmissionStatus.VALIDATED, "");
  }
  
  public void updateAsInvalid(String studyId, String uploadId, String errorMessages) {
    statusRepository.update(uploadId, studyId, SubmissionStatus.VALIDATION_ERROR, errorMessages);
  }

}
