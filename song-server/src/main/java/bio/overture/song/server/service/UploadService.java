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

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.Upload;
import bio.overture.song.server.model.analysis.AbstractAnalysis;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.enums.IdPrefix;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.repository.UploadRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.springframework.http.ResponseEntity.ok;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_CREATED;
import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerErrors.PAYLOAD_PARSING;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISSING;
import static bio.overture.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_VALIDATED;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonUtils.fromSingleQuoted;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_ID;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY;
import static bio.overture.song.server.model.enums.UploadStates.CREATED;
import static bio.overture.song.server.model.enums.UploadStates.SAVED;
import static bio.overture.song.server.model.enums.UploadStates.UPDATED;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATED;

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

  @Autowired
  private final AnalysisTypeService analysisTypeService;

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
  public ResponseEntity<String> upload(@NonNull String studyIdFromUrlPath, @NonNull String payload, boolean isAsyncValidation) {
    studyService.checkStudyExist(studyIdFromUrlPath);
    String analysisType;
    String uploadId;
    val status = JsonUtils.ObjectNode();
    status.put("status","ok");

    try {
      val analysisId= readTree(payload).at("/"+ ANALYSIS_ID).asText();
      checkStudyInPayload(studyIdFromUrlPath, payload);
      List<String> ids;

      if (isNullOrEmpty(analysisId)) {
        // Our business rules say that we always want to create a new record if no analysisId is set,
        // even if the rest of the content is duplicated.
        ids = Collections.emptyList();
      } else {
        ids = findByBusinessKey(studyIdFromUrlPath, analysisId);
      }

      if (isNull(ids) || ids.isEmpty()) {
        uploadId = id.generate(IdPrefix.UPLOAD_PREFIX);
        create(studyIdFromUrlPath, analysisId, uploadId, payload);
      } else if (ids.size() == 1) {
        uploadId = ids.get(0);
        val previousUpload = uploadRepository.findById(uploadId).get();
        status.put("status",
            format("WARNING: replaced content for analysisId '%s'",
                analysisId));
        status.put("replaced", previousUpload.getPayload());
        update(uploadId, studyIdFromUrlPath, payload);
      } else {
        throw buildServerException(getClass(), UPLOAD_ID_NOT_FOUND,
            "Multiple upload ids found for analysisId='%s', study='%s'",
            analysisId, studyIdFromUrlPath);
      }
      analysisType = readTree(payload).at("/analysisType").asText("");
    } catch (JsonProcessingException jpe){
      log.error(jpe.getMessage());
      throw buildServerException(getClass(), PAYLOAD_PARSING, "Unable parse the input payload");
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

    checkServer(uploadState == SAVED || uploadState == VALIDATED, this.getClass(),
        UPLOAD_ID_NOT_VALIDATED,
        "UploadId %s is in state '%s', but must be in state '%s' before it can be saved",
        uploadId, uploadState.getText(), VALIDATED.getText());
    val json = upload.getPayload();
    val analysis = JsonUtils.fromJson(json, AbstractAnalysis.class);
    val analysisId = analysisService.create(studyId, analysis, ignoreAnalysisIdCollisions);
    checkServer(!isNull(analysisId),this.getClass(), ANALYSIS_ID_NOT_CREATED,
        "Could not create analysisId for upload id '%s",uploadId);
    updateAsSaved(uploadId);
    val reply = fromSingleQuoted(format("{'analysisId': '%s', 'status': '%s'}", analysisId, "ok"));
    return ok(reply);
  }

  public AnalysisType register(@NonNull String analysisTypeName, @NonNull JsonNode analysisTypeSchema) {
    return analysisTypeService.register(analysisTypeName, analysisTypeSchema);
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
        .state(CREATED.getText())
        .payload(jsonPayload)
        .build();
    uploadRepository.save(upload);
  }

  private void update(@NonNull String uploadId, @NonNull String studyIdUrlParam, @NonNull String jsonPayload) {
    checkStudyInPayload(studyIdUrlParam, jsonPayload);
    val upload = unsecuredRead(uploadId);
    upload.setState(UPDATED);
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
    upload.setState(SAVED);
    uploadRepository.save(upload);
  }

  @SneakyThrows
  private static void checkStudyInPayload(String expectedStudyId, String payload){
    val payloadStudyIdOpt = getPayloadStudyId(payload);
    checkServer(payloadStudyIdOpt.isPresent(), UploadService.class, STUDY_ID_MISSING,
        "The field '%s' is missing in the payload", STUDY);
    checkServer(expectedStudyId.equals(payloadStudyIdOpt.get()), UploadService.class, STUDY_ID_MISMATCH,
        "The studyId in the URL path '%s' should match the studyId '%s' in the payload",
        expectedStudyId, payloadStudyIdOpt.get());
  }

  @SneakyThrows
  private static Optional<String> getPayloadStudyId(String payload){
    val payloadStudyId = readTree(payload).at("/"+ STUDY).asText();
    return isBlank(payloadStudyId) ? Optional.empty() : Optional.of(payloadStudyId);
  }

}
