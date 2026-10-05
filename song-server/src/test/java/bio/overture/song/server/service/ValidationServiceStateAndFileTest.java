package bio.overture.song.server.service;

import static bio.overture.song.server.model.enums.UploadStates.CREATED;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATED;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATION_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.overture.song.core.model.FileUpdateRequest;
import bio.overture.song.server.model.Upload;
import bio.overture.song.server.repository.UploadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import javax.transaction.Transactional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Tests the ValidationService methods that update an upload's validation state and that validate
 * file update requests and Score download responses with the networknt JSON schema validator.
 */
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class ValidationServiceStateAndFileTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String SEEDED_STUDY_ID = "ABC123";
  private static final String VALID_MD5 = "20de2982390c60e33452bf8736c3a9f1";

  @Autowired private ValidationService validationService;
  @Autowired private UploadRepository uploadRepository;

  private String createUpload(String uploadId) {
    val upload =
        Upload.builder()
            .uploadId(uploadId)
            .analysisId("AN1")
            .studyId(SEEDED_STUDY_ID)
            .state(CREATED.getText())
            .errors("")
            .payload("{}")
            .build();
    uploadRepository.save(upload);
    return uploadId;
  }

  private Upload readUpload(String uploadId) {
    return uploadRepository.findById(uploadId).get();
  }

  @Test
  public void update_withoutErrors_marksValidated() {
    val uploadId = createUpload("upload-valid");
    validationService.update(uploadId, null);
    val upload = readUpload(uploadId);
    assertEquals(VALIDATED.getText(), upload.getState());
    // An empty error string splits into a single empty element.
    assertEquals(List.of(""), upload.getErrors());
  }

  @Test
  public void update_withErrors_marksValidationErrorAndSplitsErrors() {
    val uploadId = createUpload("upload-invalid");
    validationService.update(uploadId, "first error|second error");
    val upload = readUpload(uploadId);
    assertEquals(VALIDATION_ERROR.getText(), upload.getState());
    assertEquals(List.of("first error", "second error"), upload.getErrors());
  }

  @Test
  public void updateAsInvalid_marksValidationError() {
    val uploadId = createUpload("upload-as-invalid");
    validationService.updateAsInvalid(uploadId, "broken");
    val upload = readUpload(uploadId);
    assertEquals(VALIDATION_ERROR.getText(), upload.getState());
    assertEquals(List.of("broken"), upload.getErrors());
  }

  @Test
  public void update_unknownUpload_doesNothing() {
    validationService.update("no-such-upload", "ignored");
    assertFalse(uploadRepository.existsById("no-such-upload"));
  }

  @Test
  public void validateFileData_validRequest_noErrors() {
    val info = MAPPER.createObjectNode().put("key", "value");
    val request =
        FileUpdateRequest.builder()
            .fileSize(10L)
            .fileMd5sum(VALID_MD5)
            .fileAccess("open")
            .dataType("SOME_TYPE")
            .info(info)
            .build();
    assertTrue(validationService.validate(request).isEmpty());
  }

  @Test
  public void validateFileData_emptyRequest_noErrors() {
    assertTrue(validationService.validate(FileUpdateRequest.builder().build()).isEmpty());
  }

  @Test
  public void validateFileData_invalidMd5_error() {
    val result = validationService.validate(FileUpdateRequest.builder().fileMd5sum("xyz").build());
    assertTrue(result.isPresent());
    assertTrue(result.get().contains("fileMd5sum"), result.get());
  }

  @Test
  public void validateFileData_invalidAccess_error() {
    val result =
        validationService.validate(FileUpdateRequest.builder().fileAccess("public").build());
    assertTrue(result.isPresent());
    assertTrue(result.get().contains("fileAccess"), result.get());
  }

  @Test
  public void validateFileData_zeroSize_error() {
    val result = validationService.validate(FileUpdateRequest.builder().fileSize(0L).build());
    assertTrue(result.isPresent());
    assertTrue(result.get().contains("fileSize"), result.get());
  }

  private static ObjectNode downloadResponse(String objectId, Long objectSize, String objectMd5) {
    val response = MAPPER.createObjectNode();
    response.put("objectId", objectId);
    if (objectSize != null) {
      response.put("objectSize", objectSize);
    }
    response.put("objectMd5", objectMd5);
    return response;
  }

  @Test
  public void validateStorageDownloadResponse_valid_noErrors() {
    val response = downloadResponse("a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d", 100L, VALID_MD5);
    assertTrue(validationService.validateStorageDownloadResponse(response).isEmpty());
  }

  @Test
  public void validateStorageDownloadResponse_nullMd5_noErrors() {
    val response = downloadResponse("a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d", 100L, null);
    assertTrue(validationService.validateStorageDownloadResponse(response).isEmpty());
  }

  @Test
  public void validateStorageDownloadResponse_missingSize_error() {
    val response = downloadResponse("a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d", null, VALID_MD5);
    val result = validationService.validateStorageDownloadResponse(response);
    assertTrue(result.isPresent());
    assertTrue(result.get().contains("objectSize"), result.get());
  }

  @Test
  public void validateStorageDownloadResponse_invalidMd5_error() {
    val response = downloadResponse("a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d", 100L, "not-an-md5");
    val result = validationService.validateStorageDownloadResponse(response);
    assertTrue(result.isPresent());
    assertTrue(result.get().contains("objectMd5"), result.get());
  }

  @Test
  public void validateStorageDownloadResponse_invalidObjectId_error() {
    val response = downloadResponse("-bad id-", 100L, VALID_MD5);
    val result = validationService.validateStorageDownloadResponse(response);
    assertTrue(result.isPresent());
    assertTrue(result.get().contains("objectId"), result.get());
  }
}
