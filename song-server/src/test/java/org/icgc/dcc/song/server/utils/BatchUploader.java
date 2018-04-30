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

package org.icgc.dcc.song.server.utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.util.Joiners;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.service.StudyService;
import org.icgc.dcc.song.server.service.UploadService;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.util.Sets.newHashSet;
import static org.icgc.dcc.common.core.util.Joiners.NEWLINE;
import static org.icgc.dcc.song.server.model.enums.UploadStates.VALIDATED;
import static org.icgc.dcc.song.server.model.enums.UploadStates.VALIDATION_ERROR;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;

@Slf4j
@RequiredArgsConstructor
public class BatchUploader {

  @NonNull private final StudyService studyService;
  @NonNull private final UploadService uploadService;

  public void process(@NonNull BatchUpload batchUpload){
    val studyId = batchUpload.getStudyId();
    initStudy(studyId);
    uploadBatch(batchUpload, false);
    val numErrored = pollStatus(batchUpload);
    checkState(numErrored == 0,
        "There were errors with the uploads: \n%s",
        NEWLINE.join(batchUpload.getUploadMap().values()));
    saveBatch(batchUpload, false);
    log.info("SAVED Uploads: {}", Joiners.NEWLINE.join(batchUpload.getSavedUplouds()));
    log.info("ERRORED Uploads: {}", Joiners.NEWLINE.join(batchUpload.getErroredUploads()));
    log.info("done");
  }

  private void uploadBatch(BatchUpload batchUpload, boolean isAsync){
    val studyId = batchUpload.getStudyId();
    for (val payload: batchUpload.getPayloads()){
      val uploadResponse = uploadService.upload(studyId, payload, isAsync );
      val uploadId = fromStatus(uploadResponse, "uploadId");
      val upload = uploadService.read(uploadId);
      batchUpload.addUpload(upload);
    }
  }

  private int pollStatus(BatchUpload batchUpload){
    Set<String> validated = newHashSet();
    Set<String> validationError = newHashSet();
    val total = batchUpload.getUploadMap().keySet().size();


    while(validated.size() + validationError.size() < total ){
      for (val upload : batchUpload.getUploadMap().values()){
        val uploadId = upload.getUploadId();
        if (!validated.contains(uploadId) && !validationError.contains(uploadId)){
          val newUpload = uploadService.read(uploadId);
          batchUpload.updateUpload(newUpload);
          if (resolveState(newUpload.getState()) == VALIDATION_ERROR){
            validationError.add(newUpload.getUploadId());
          } else if (resolveState(newUpload.getState()) == VALIDATED){
            validated.add(newUpload.getUploadId());
          }
        }
      }
    }
    return validationError.size();
  }

  private void saveBatch(BatchUpload batchUpload, boolean ignoreAnalysisIdCollisions){
    val studyId = batchUpload.getStudyId();
    for (val upload : batchUpload.getValidatedUploads()){
      val saveResponse = uploadService.save(studyId, upload.getUploadId(), ignoreAnalysisIdCollisions );
      val analysisId = fromStatus(saveResponse, "analysisId");
      upload.setAnalysisId(analysisId);
    }
  }

  @SneakyThrows
  private static String fromStatus( ResponseEntity<String> response, String key) {
    val value = JsonUtils.readTree(response.getBody()).at("/"+key).asText("");
    return value;
  }

  private void initStudy(String studyId){
    if (!studyService.isStudyExist(studyId)){
      studyService.saveStudy(Study.builder()
          .studyId(studyId)
          .name("")
          .organization("")
          .description("")
          .build());
    }
  }

}
