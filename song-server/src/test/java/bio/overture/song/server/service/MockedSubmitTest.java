/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.PAYLOAD_PARSING;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_MISMATCH;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.Responses.OK;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.VARIANT_CALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.SubmitResponse;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.repository.UploadRepository;
import bio.overture.song.server.service.id.IdService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MockedSubmitTest {

  /** Dependencies */
  @Mock private StudyService studyService;

  @Mock private AnalysisService analysisService;
  @Mock private ValidationService validationService;
  @Mock private IdService idService;
  @Mock private UploadRepository uploadRepository;

  /** DUT */
  @InjectMocks private UploadService uploadService;

  @Test
  public void submit_MissingAnalysisType_MalformedParameter() {
    // Setup
    doNothing().when(studyService).checkStudyExist(anyString());

    // Verify
    assertSongError(() -> uploadService.submit("anyStudy", "{}"), MALFORMED_PARAMETER);
  }

  @Test
  public void submit_MissingAnalysisTypeName_MalformedParameter() {
    // Setup
    doNothing().when(studyService).checkStudyExist(anyString());
    when(validationService.validateAnalysisTypeVersion(isA(AnalysisTypeId.class)))
        .thenCallRealMethod();

    val payload =
        JsonUtils.toJson(
            Payload.builder()
                .studyId("anyStudy")
                .analysisType(AnalysisTypeId.builder().build())
                .build());

    // Verify
    assertSongError(() -> uploadService.submit("anyStudy", payload), MALFORMED_PARAMETER);
  }

  @Test
  public void submit_StudyDne_StudyIdDoesNotExist() {
    // Setup
    doThrow(buildServerException(getClass(), STUDY_ID_DOES_NOT_EXIST, "study dne"))
        .when(studyService)
        .checkStudyExist(anyString());

    // Verify
    assertSongError(
        () -> uploadService.submit("anyStudy", "anyAnalysisId"), STUDY_ID_DOES_NOT_EXIST);
    verify(validationService, never()).validate(isA(JsonNode.class));
    verify(analysisService, never()).create(anyString(), isA(Payload.class));
  }

  @Test
  public void submit_malformedPayload_PayloadParsingError() {
    // Setup
    doNothing().when(studyService).checkStudyExist(anyString());

    // Verify
    assertSongError(() -> uploadService.submit("anyStudy", "non json format"), PAYLOAD_PARSING);
    verify(validationService, never()).validate(isA(JsonNode.class));
    verify(analysisService, never()).create(anyString(), isA(Payload.class));
  }

  @Test
  public void submit_invalidPayload_SchemaViolation() {
    // Setup
    val studyId = "anyStudy";
    doNothing().when(studyService).checkStudyExist(anyString());
    when(validationService.validate(isA(JsonNode.class)))
        .thenReturn(Optional.of("there was an error"));

    // Create an invalid payload and not a malformed one
    val invalidPayload =
        toJson(
            Payload.builder()
                .studyId(studyId)
                .analysisType(
                    AnalysisTypeId.builder().name(VARIANT_CALL.getAnalysisTypeName()).build())
                .build());

    // Verify
    assertSongError(() -> uploadService.submit(studyId, invalidPayload), SCHEMA_VIOLATION);
    verify(validationService, times(1)).validate(isA(JsonNode.class));
    verify(analysisService, never()).create(anyString(), isA(Payload.class));
  }

  @Test
  public void submit_mismatchingStudies_StudyIdMismatch() {
    // Setup
    val study1 = "study1";
    val study2 = "study2";
    doNothing().when(studyService).checkStudyExist(anyString());
    when(validationService.validate(isA(JsonNode.class))).thenReturn(Optional.empty());
    val payloadString =
        toJson(
            Payload.builder()
                .studyId(study1)
                .analysisType(
                    AnalysisTypeId.builder().name(VARIANT_CALL.getAnalysisTypeName()).build())
                .build());

    // Verify
    assertNotEquals(study1, study2);
    assertSongError(() -> uploadService.submit(study2, payloadString), STUDY_ID_MISMATCH);
    verify(validationService, times(1)).validate(isA(JsonNode.class));
    verify(analysisService, never()).create(anyString(), isA(Payload.class));
  }

  @Test
  public void submit_validPayload_Success() {
    // Setup
    val study = "study1";
    val analysisId = "analysis123";
    val payload =
        Payload.builder()
            .studyId(study)
            .analysisType(AnalysisTypeId.builder().name(VARIANT_CALL.getAnalysisTypeName()).build())
            .build();

    doNothing().when(studyService).checkStudyExist(anyString());
    when(validationService.validate(isA(JsonNode.class))).thenReturn(Optional.empty());

    val payloadString = toJson(payload);
    when(analysisService.create(study, payload)).thenReturn(analysisId);
    val expectedSubmitResponse = SubmitResponse.builder().analysisId(analysisId).status(OK).build();

    // Verify
    val actualSubmitResponse = uploadService.submit(study, payloadString);
    assertEquals(expectedSubmitResponse, actualSubmitResponse);
    verify(validationService, times(1)).validate(isA(JsonNode.class));
    verify(analysisService, times(1)).create(anyString(), isA(Payload.class));
  }
}
