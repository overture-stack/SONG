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

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_INCORRECT_VERSION;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.PAYLOAD_PARSING;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISSING;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.Responses.OK;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY;
import static java.util.Objects.isNull;

import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.dto.SubmitResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UploadService {

  private final ValidationService validator;
  private final AnalysisService analysisService;
  private final StudyService studyService;

  @Autowired
  public UploadService(
      @NonNull ValidationService validator,
      @NonNull AnalysisService analysisService,
      @NonNull StudyService studyService) {
    this.validator = validator;
    this.analysisService = analysisService;
    this.studyService = studyService;
  }

  @Transactional
  public SubmitResponse submit(
      @NonNull String studyId, String payloadString, final boolean ignoreAnalysisIdCollisions) {
    // Check study exists
    studyService.checkStudyExist(studyId);

    // Parse JSON payload
    val payloadJson = parsePayload(payloadString);

    // Validate JSON Payload
    validatePayload(payloadJson);

    // Deserialize JSON payload to Payload DTO
    val payload = fromJson(payloadJson, Payload.class);

    // Check that the Payload's studyId matches the request studyId
    checkStudyInPayload(studyId, payload);

    // Create the analysis
    val analysisId = analysisService.create(studyId, payload, ignoreAnalysisIdCollisions);
    return SubmitResponse.builder().analysisId(analysisId).status(OK).build();
  }

  private void checkAnalysisTypeVersion(@NonNull Payload payload) {
    checkServer(
        !isNull(payload.getAnalysisType()),
        getClass(),
        MALFORMED_PARAMETER,
        "The analysisType field cannot be null");
    val analysisTypeId = payload.getAnalysisType();
    val errors = validator.validateAnalysisTypeVersion(analysisTypeId);
    checkServer(isNull(errors), getClass(), ANALYSIS_TYPE_INCORRECT_VERSION, errors);
  }

  private JsonNode parsePayload(String payloadString) {
    try {
      val payloadJson = readTree(payloadString);
      val payload = fromJson(payloadJson, Payload.class);
      checkAnalysisTypeVersion(payload);
      return payloadJson;
    } catch (IOException e) {
      log.error(e.getMessage());
      throw buildServerException(
          getClass(),
          PAYLOAD_PARSING,
          "Unable to read the input payload: " + payloadString.replaceAll("%", "%%"));
    }
  }

  private void validatePayload(JsonNode payloadJson) {
    // Validate payload format and content
    val error = validator.validate(payloadJson);
    if (error.isPresent()) {
      val message = error.get();
      throw buildServerException(getClass(), SCHEMA_VIOLATION, message);
    }
  }

  @SneakyThrows
  private static void checkStudyInPayload(String expectedStudyId, Payload payload) {
    val payloadStudyId = payload.getStudy();
    checkServer(
        !isNull(payloadStudyId),
        UploadService.class,
        STUDY_ID_MISSING,
        "The field '%s' is missing in the payload",
        STUDY);
    checkServer(
        expectedStudyId.equals(payloadStudyId),
        UploadService.class,
        STUDY_ID_MISMATCH,
        "The studyId in the URL path '%s' should match the studyId '%s' in the payload",
        expectedStudyId,
        payloadStudyId);
  }
}
