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

import static bio.overture.song.core.exceptions.ServerErrors.*;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.Responses.OK;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.VARIANT_CALL;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.SubmitResponse;
import bio.overture.song.core.model.enums.FileTypes;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.repository.UploadRepository;
import bio.overture.song.server.service.id.IdService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class MockedSubmitTest {

  /** Dependencies */
  @Mock private StudyService studyService;

  @Mock private AnalysisService analysisService;
  @Mock private ValidationService validationService;
  @Mock private IdService idService;
  @Mock private UploadRepository uploadRepository;
  @Mock private SampleService sampleService;
  @Mock private SpecimenService specimenService;
  @Mock private DonorService donorService;
  @Spy private List<VerificationService> verificationServices = new ArrayList<>();

  /** DUT */
  @InjectMocks private SubmitService submitService;

  @Test
  public void submit_MissingAnalysisType_MalformedParameter() {
    // Setup
    doNothing().when(studyService).checkStudyExist(anyString());

    // Verify
    assertSongError(() -> submitService.submit("anyStudy", "{}"), MALFORMED_PARAMETER);
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
    assertSongError(() -> submitService.submit("anyStudy", payload), MALFORMED_PARAMETER);
  }

  @Test
  public void submit_StudyDne_StudyIdDoesNotExist() {
    // Setup
    doThrow(buildServerException(getClass(), STUDY_ID_DOES_NOT_EXIST, "study dne"))
        .when(studyService)
        .checkStudyExist(anyString());

    // Verify
    assertSongError(
        () -> submitService.submit("anyStudy", "anyAnalysisId"), STUDY_ID_DOES_NOT_EXIST);
    verify(validationService, never()).validate(isA(JsonNode.class));
    verify(analysisService, never()).create(anyString(), isA(Payload.class));
  }

  @Test
  public void submit_malformedPayload_PayloadParsingError() {
    // Setup
    doNothing().when(studyService).checkStudyExist(anyString());

    // Verify
    assertSongError(() -> submitService.submit("anyStudy", "non json format"), PAYLOAD_PARSING);
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
    assertSongError(() -> submitService.submit(studyId, invalidPayload), SCHEMA_VIOLATION);
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
    assertSongError(() -> submitService.submit(study2, payloadString), STUDY_ID_MISMATCH);
    verify(validationService, times(1)).validate(isA(JsonNode.class));
    verify(analysisService, never()).create(anyString(), isA(Payload.class));
  }

  @Test
  public void submit_validPayload_Success() {
    // Setup
    val study = "study1";
    val analysisId = "analysis123";
    val sample = new CompositeEntity();
    sample.setDonor(new Donor());
    sample.setSpecimen(new Specimen());
    sample.getSpecimen().setSubmitterSpecimenId("abcd");
    sample.getDonor().setSubmitterDonorId("DO1234");
    val samples = List.of(sample);
    val payload =
        Payload.builder()
            .studyId(study)
            .samples(samples)
            .analysisType(AnalysisTypeId.builder().name(VARIANT_CALL.getAnalysisTypeName()).build())
            .build();

    doNothing().when(studyService).checkStudyExist(anyString());
    when(validationService.validate(isA(JsonNode.class))).thenReturn(Optional.empty());

    val payloadString = toJson(payload);
    when(analysisService.create(study, payload)).thenReturn(analysisId);
    val expectedSubmitResponse = SubmitResponse.builder().analysisId(analysisId).status(OK).build();

    // Verify
    val actualSubmitResponse = submitService.submit(study, payloadString);
    assertEquals(expectedSubmitResponse, actualSubmitResponse);
    verify(validationService, times(1)).validate(isA(JsonNode.class));
    verify(analysisService, times(1)).create(anyString(), isA(Payload.class));
  }

  // TODO: Mock these
  @Test
  public void test_real_verification() {
    val validationFailedMessage =
        "[SubmitService::payload.verification.failed] - Validation Errors: ";
    val study = "study1";
    val payload = samplePayload(study);
    String msg = testValidateFailures(toJson(payload), true);

    val expected = "[Analysis has no files section!]";
    assertEquals(validationFailedMessage + expected, msg);

    val files = new ArrayList<FileEntity>();
    payload.setFiles(files);
    val msg2 = testValidateFailures(toJson(payload), true);
    val expected2 = "[You must include at least two files for a variant call]";
    assertEquals(validationFailedMessage + expected2, msg2);

    val files2 = new ArrayList<FileEntity>();
    val f1 = new FileEntity();
    f1.setFileType(FileTypes.BAM);
    f1.setFileName("test.bam.gz");
    f1.setFileMd5sum("Bad md5sum by the way");

    val f2 = new FileEntity();
    f2.setFileType(FileTypes.FASTA);
    f2.setFileName("test.fasta");

    files2.add(f1);
    files2.add(f2);
    payload.setFiles(files2);
    val request = toJson(payload);
    val reply = testValidateFailures(request, true);
    val expected3 =
        "[You must include an index file for file \"test.bam.gz\", "
            + "You must include the md5sum for file \"test.fasta\"]";
    assertEquals(validationFailedMessage + expected3, reply);

    val files3 = new ArrayList();

    files3.add(f1);
    f2.setFileMd5sum("fake md5 sum");

    val f3 = new FileEntity();
    f3.setFileType(FileTypes.BAI);
    f3.setFileName("test.bai.gz");
    f3.setFileMd5sum("fake sum");

    files3.add(f3);

    payload.setFiles(files3);
    val request4 = toJson(payload);
    val reply4 = testValidateFailures(request4, false);
    assertEquals("SubmitResponse(analysisId=null, status=OK)", reply4);

    val files4 = new ArrayList();
    files4.add(f1);
    files4.add(f2);
    files4.add(f3);
    val f4 = new FileEntity();
    f4.setFileName("test.x.cram");
    f4.setFileType(FileTypes.XML);
    f4.setFileMd5sum("fictional");
    files4.add(f4);

    payload.setFiles(files4);
    val request5 = toJson(payload);
    val reply5 = testValidateFailures(request5, true);
    val expected5 = "Verifier exception: Exception('Demonstrate exception handling')";
    val expectedPrefix="[RestVerificationService::payload.verification.failed] - ";
    assertEquals(expectedPrefix+ expected5, reply5);
  }

  String testValidateFailures(String payload, boolean expectFailure) {
    val s = getSubmitService();
    ServerException serverException = null;
    String result = "";
    try {
      result = s.submit("study1", payload).toString();
    } catch (ServerException e) {
      serverException = e;
    }
    if (expectFailure) {
      if (serverException == null) {
        val breakpoint = 42;
      }
      assertNotNull(serverException);
      return serverException.getMessage();
    }
    return result;
  }

  SubmitService getSubmitService() {
    List<VerificationService> verificationServices =
        List.of(new RestVerificationService(new RestTemplate(), "http://localhost:8080"));
    return new SubmitService(
        validationService, analysisService, studyService, verificationServices);
  }

  Payload samplePayload(String study) {
    val sample = new CompositeEntity();
    sample.setDonor(new Donor());
    sample.setSpecimen(new Specimen());
    sample.getSpecimen().setSubmitterSpecimenId("abcd");
    sample.getDonor().setSubmitterDonorId("DO1234");
    val samples = List.of(sample);
    return Payload.builder()
        .studyId(study)
        .samples(samples)
        .analysisType(AnalysisTypeId.builder().name(VARIANT_CALL.getAnalysisTypeName()).build())
        .build();
  }
}
