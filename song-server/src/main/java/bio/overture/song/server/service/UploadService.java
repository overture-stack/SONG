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

import bio.overture.song.server.model.Upload;
import bio.overture.song.server.model.analysis.AbstractAnalysis;
import bio.overture.song.server.model.enums.IdPrefix;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.repository.UploadRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_CREATED;
import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerErrors.PAYLOAD_PARSING;
import static bio.overture.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_VALIDATED;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonUtils.fromSingleQuoted;
import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@Service
@Slf4j
public class UploadService {

  @Autowired
  private final IdService id;
  @Autowired
  private final ValidationService validator;
  @Autowired
  private final AnalysisService analysisService;
  @Autowired
  private final UploadRepository uploadRepository;
  @Autowired
  private final StudyService studyService;

  public boolean isUploadExist(@NonNull String uploadId){
    return uploadRepository.existsById(uploadId);
  }

  public Upload securedRead(@NonNull String studyId, String uploadId) {
    checkUploadRelatedToStudy(studyId, uploadId);
    return unsecuredRead(uploadId);
  }

  public void checkUploadRelatedToStudy(@NonNull String studyId, @NonNull String id){
    val numUploads = uploadRepository.countAllByStudyIdAndUploadId(studyId, id);
    if (numUploads < 1){
      studyService.checkStudyExist(studyId);
      val upload = unsecuredRead(id);
      throw buildServerException(getClass(), ENTITY_NOT_RELATED_TO_STUDY,
          "The uploadId '%s' is not related to the input studyId '%s'. It is actually related to studyId '%s'",
          id, studyId, upload.getStudyId());
    }
  }

  @Transactional
  @SneakyThrows
  public ResponseEntity<String> upload(@NonNull String studyId, @NonNull String payload, boolean isAsyncValidation) {
    studyService.checkStudyExist(studyId);
    String analysisType;
    String uploadId;
    val status = JsonUtils.ObjectNode();
    status.put("status","ok");

    try {
      val analysisId=JsonUtils.readTree(payload).at("/analysisId").asText();
      List<String> ids;

      if (isNullOrEmpty(analysisId)) {
        // Our business rules say that we always want to create a new record if no analysisId is set,
        // even if the rest of the content is duplicated.
        ids = Collections.emptyList();
      } else {
        ids = findByBusinessKey(studyId, analysisId);
      }

      if (isNull(ids) || ids.isEmpty()) {
        uploadId = id.generate(IdPrefix.UPLOAD_PREFIX);
        create(studyId, analysisId, uploadId, payload);
      } else if (ids.size() == 1) {
        uploadId = ids.get(0);
        val previousUpload = uploadRepository.findById(uploadId).get();
        status.put("status",
            format("WARNING: replaced content for analysisId '%s'",
                analysisId));
        status.put("replaced", previousUpload.getPayload());
        update(uploadId, payload);

      } else {
        throw buildServerException(getClass(), UPLOAD_ID_NOT_FOUND,
            "Multiple upload ids found for analysisId='%s', study='%s'",
            analysisId, studyId);
      }
      analysisType = JsonUtils.readTree(payload).at("/analysisType").asText("");
    } catch (JsonProcessingException jpe){
      log.error(jpe.getCause().getMessage());
      throw buildServerException(getClass(), PAYLOAD_PARSING,
          "Unable parse the input payload: %s ",payload);
    }

    if (isAsyncValidation){
      validator.asyncValidate(uploadId, payload, analysisType); // Asynchronous operation.
    } else {
      validator.syncValidate(uploadId, payload, analysisType); // Synchronous operation
    }
    status.put("uploadId", uploadId);
    return ok(status.toString());
  }

  @Transactional
  public ResponseEntity<String> save(@NonNull String studyId, @NonNull String uploadId,
      final boolean ignoreAnalysisIdCollisions) {
    val upload = securedRead(studyId, uploadId);
    val uploadState = UploadStates.resolveState(upload.getState());

    ServerException.checkServer(uploadState == UploadStates.SAVED || uploadState == UploadStates.VALIDATED, this.getClass(),
        UPLOAD_ID_NOT_VALIDATED,
        "UploadId %s is in state '%s', but must be in state '%s' before it can be saved",
        uploadId, uploadState.getText(), UploadStates.VALIDATED.getText());
    val json = upload.getPayload();
    val analysis = JsonUtils.fromJson(json, AbstractAnalysis.class);
    val analysisId = analysisService.create(studyId, analysis, ignoreAnalysisIdCollisions);
    checkServer(!isNull(analysisId),this.getClass(), ANALYSIS_ID_NOT_CREATED,
        "Could not create analysisId for upload id '%s",uploadId);
    updateAsSaved(uploadId);
    val reply = fromSingleQuoted(format("{'analysisId': '%s', 'status': '%s'}", analysisId, "ok"));
    return ok(reply);
  }

  private Upload unsecuredRead(@NonNull String uploadId) {
    val uploadResult = uploadRepository.findById(uploadId);
    checkServer(uploadResult.isPresent(), this.getClass(), UPLOAD_ID_NOT_FOUND,
        "The uploadId '%s' was not found", uploadId);
    return uploadResult.get();
  }

  private void create(@NonNull String studyId, String analysisId, @NonNull String uploadId,
                      @NonNull String jsonPayload) {
    val upload = Upload.builder()
        .uploadId(uploadId)
        .analysisId(analysisId)
        .studyId(studyId)
        .state(UploadStates.CREATED.getText())
        .payload(jsonPayload)
        .build();
    uploadRepository.save(upload);
  }

  private void update(@NonNull String uploadId, @NonNull String jsonPayload) {
    val upload = unsecuredRead(uploadId);
    upload.setState(UploadStates.UPDATED);
    upload.setPayload(jsonPayload);
    uploadRepository.save(upload);
  }

  private  List<String> findByBusinessKey(@NonNull String studyId, @NonNull String analysisId){
    return uploadRepository.findAllByStudyIdAndAnalysisId(studyId, analysisId).stream()
        .map(Upload::getUploadId)
        .collect(toImmutableList());
  }

  private void updateAsSaved(@NonNull String uploadId) {
    val upload = unsecuredRead(uploadId);
    upload.setState(UploadStates.SAVED);
    uploadRepository.save(upload);
  }

}
