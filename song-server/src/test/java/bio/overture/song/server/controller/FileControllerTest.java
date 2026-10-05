package bio.overture.song.server.controller;

import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.INVALID_FILE_UPDATE_REQUEST;
import static bio.overture.song.core.model.enums.FileUpdateTypes.CONTENT_UPDATE;
import static bio.overture.song.core.model.enums.FileUpdateTypes.METADATA_UPDATE;
import static bio.overture.song.core.model.enums.FileUpdateTypes.NO_UPDATE;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.EndpointTester.createEndpointTester;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import bio.overture.song.core.model.FileUpdateRequest;
import bio.overture.song.core.model.FileUpdateResponse;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.utils.EndpointTester;
import bio.overture.song.server.utils.web.ResponseOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests GET, PUT and DELETE /studies/{studyId}/files/{id}.
 *
 * <p>Not transactional: each request commits on its own, as in production. Inside one test
 * transaction the file update is not visible to the following read.
 */
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileControllerTest {

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private StudyService studyService;
  @Autowired private AnalysisService analysisService;

  private EndpointTester endpointTester;
  private String studyId;
  private String otherStudyId;
  private List<FileEntity> files;

  @BeforeEach
  public void beforeEachTest() {
    this.endpointTester =
        createEndpointTester(webAppContextSetup(webApplicationContext).build(), true);
    val randomGenerator = createRandomGenerator(getClass().getSimpleName());
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    this.studyId = studyGenerator.createRandomStudy();
    this.otherStudyId = studyGenerator.createRandomStudy();
    this.files =
        createAnalysisGenerator(studyId, analysisService, randomGenerator)
            .createDefaultRandomVariantCallAnalysis()
            .getFiles();
  }

  private ResponseOption getFileAnd(String fileStudyId, String objectId) {
    return endpointTester
        .initWebRequest()
        .endpoint("studies/%s/files/%s", fileStudyId, objectId)
        .getAnd();
  }

  private ResponseOption putFileAnd(String objectId, FileUpdateRequest request) {
    return endpointTester
        .initWebRequest()
        .endpoint("studies/%s/files/%s", studyId, objectId)
        .body(request)
        .putAnd();
  }

  private ResponseOption deleteFilesAnd(String fileStudyId, String commaSeparatedObjectIds) {
    return endpointTester
        .initWebRequest()
        .endpoint("studies/%s/files/%s", fileStudyId, commaSeparatedObjectIds)
        .deleteAnd();
  }

  @Test
  public void read_existingFile_returnsFile() {
    val expected = files.get(0);
    val actual = getFileAnd(studyId, expected.getObjectId()).extractOneEntity(FileEntity.class);
    assertEquals(expected.getObjectId(), actual.getObjectId());
    assertEquals(expected.getAnalysisId(), actual.getAnalysisId());
    assertEquals(expected.getFileName(), actual.getFileName());
    assertEquals(expected.getFileSize(), actual.getFileSize());
    assertEquals(expected.getFileMd5sum(), actual.getFileMd5sum());
    assertEquals(expected.getFileAccess(), actual.getFileAccess());
    assertEquals(expected.getFileType(), actual.getFileType());
    assertEquals(expected.getDataType(), actual.getDataType());
    assertEquals(studyId, actual.getStudyId());
  }

  @Test
  public void read_unknownFile_notFound() {
    getFileAnd(studyId, "no-such-file").assertServerError(FILE_NOT_FOUND);
  }

  @Test
  public void read_fileOfAnotherStudy_notRelated() {
    getFileAnd(otherStudyId, files.get(0).getObjectId())
        .assertServerError(ENTITY_NOT_RELATED_TO_STUDY);
  }

  @Test
  public void update_md5_contentUpdateIsStored() {
    val objectId = files.get(0).getObjectId();
    val newMd5 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    val response =
        putFileAnd(objectId, FileUpdateRequest.builder().fileMd5sum(newMd5).build())
            .extractOneEntity(FileUpdateResponse.class);
    assertEquals(CONTENT_UPDATE, response.getFileUpdateType());
    assertEquals(files.get(0).getFileMd5sum(), response.getOriginalFile().getFileMd5sum());
    assertEquals(
        newMd5, getFileAnd(studyId, objectId).extractOneEntity(FileEntity.class).getFileMd5sum());
  }

  @Test
  public void update_info_metadataUpdateIsStored() {
    val objectId = files.get(0).getObjectId();
    val info = new ObjectMapper().createObjectNode().put("updated", "metadata");
    val response =
        putFileAnd(objectId, FileUpdateRequest.builder().info(info).build())
            .extractOneEntity(FileUpdateResponse.class);
    assertEquals(METADATA_UPDATE, response.getFileUpdateType());
    assertEquals(info, getFileAnd(studyId, objectId).extractOneEntity(FileEntity.class).getInfo());
  }

  @Test
  public void update_sameValues_noUpdate() {
    val original = files.get(0);
    val response =
        putFileAnd(
                original.getObjectId(),
                FileUpdateRequest.builder()
                    .fileMd5sum(original.getFileMd5sum())
                    .fileSize(original.getFileSize())
                    .build())
            .extractOneEntity(FileUpdateResponse.class);
    assertEquals(NO_UPDATE, response.getFileUpdateType());
  }

  @Test
  public void update_invalidMd5_rejected() {
    putFileAnd(files.get(0).getObjectId(), FileUpdateRequest.builder().fileMd5sum("xyz").build())
        .assertServerError(INVALID_FILE_UPDATE_REQUEST);
  }

  @Test
  public void delete_singleFile_returnsOkAndRemovesFile() {
    val objectId = files.get(0).getObjectId();
    val response = deleteFilesAnd(studyId, objectId);
    response.assertOk();
    assertEquals("OK", response.getResponse().getBody());
    getFileAnd(studyId, objectId).assertServerError(FILE_NOT_FOUND);
  }

  @Test
  public void delete_commaSeparatedFiles_removesAll() {
    val firstObjectId = files.get(0).getObjectId();
    val secondObjectId = files.get(1).getObjectId();
    deleteFilesAnd(studyId, firstObjectId + "," + secondObjectId).assertOk();
    getFileAnd(studyId, firstObjectId).assertServerError(FILE_NOT_FOUND);
    getFileAnd(studyId, secondObjectId).assertServerError(FILE_NOT_FOUND);
  }

  @Test
  public void delete_fileOfAnotherStudy_notRelated() {
    deleteFilesAnd(otherStudyId, files.get(0).getObjectId())
        .assertServerError(ENTITY_NOT_RELATED_TO_STUDY);
  }
}
