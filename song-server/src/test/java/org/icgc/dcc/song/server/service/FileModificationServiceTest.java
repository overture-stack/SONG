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
package org.icgc.dcc.song.server.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.converter.FileConverter;
import org.icgc.dcc.song.server.model.entity.FileEntity;
import org.icgc.dcc.song.core.model.file.FileUpdateRequest;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ILLEGAL_FILE_UPDATE_REQUEST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INVALID_FILE_UPDATE_REQUEST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.core.model.file.FileUpdateRequest.createFileUpdateRequest;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.PUBLISHED;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.resolveAnalysisState;
import static org.icgc.dcc.song.core.model.enums.FileUpdateTypes.CONTENT_UPDATE;
import static org.icgc.dcc.song.core.model.enums.FileUpdateTypes.METADATA_UPDATE;
import static org.icgc.dcc.song.core.model.enums.FileUpdateTypes.NO_UPDATE;
import static org.icgc.dcc.song.core.model.enums.FileUpdateTypes.resolveFileUpdateType;
import static org.icgc.dcc.song.server.service.FileModificationService.doUnpublish;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_ANALYSIS_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_FILE_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.securestudy.impl.SecureFileTester.createSecureFileTester;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class FileModificationServiceTest {

  @Autowired
  FileService fileService;
  @Autowired
  StudyService studyService;
  @Autowired
  AnalysisService analysisService;

  @Autowired
  FileModificationService fileModificationService;

  @Autowired
  FileRepository fileRepository;

  private String uniqueMd5;
  private static final FileConverter FILE_CONVERTER = Mappers.getMapper(FileConverter.class);
  private final RandomGenerator randomGenerator = createRandomGenerator(FileModificationServiceTest.class.getSimpleName());

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    assertThat(analysisService.isAnalysisExist(DEFAULT_ANALYSIS_ID)).isTrue();
    assertThat(fileService.isFileExist(DEFAULT_FILE_ID)).isTrue();
    this.uniqueMd5 = randomGenerator.generateRandomMD5();
  }

  @Test
  public void testDoPublish(){
    assertThat(doUnpublish(NO_UPDATE)).isFalse();
    assertThat(doUnpublish(METADATA_UPDATE)).isFalse();
    assertThat(doUnpublish(CONTENT_UPDATE)).isTrue();
  }

  @Test
  @Transactional
  public void testCheckFileUnrelatedToStudy(){
    val secureFileTester = createSecureFileTester(randomGenerator, studyService, fileService, analysisService);

    val randomFileUpdateRequest = createFileUpdateRequest(
        (long)randomGenerator.generateRandomIntRange(1,100000),
        randomGenerator.generateRandomMD5(),
        randomGenerator.randomEnum(AccessTypes.class).toString(),
        object().end());
    secureFileTester.runSecureTest((s,f) -> fileModificationService.securedFileWithAnalysisUpdate(s, f, randomFileUpdateRequest));
  }
  
  @Test
  @Transactional
  public void testFileUpdateWithSuppressedAnalysis(){
    analysisService.securedUpdateState(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID, SUPPRESSED);
    val dummyRequest = new FileUpdateRequest();
    assertSongError(
        () ->  fileModificationService.securedFileWithAnalysisUpdate(DEFAULT_STUDY_ID, DEFAULT_FILE_ID, dummyRequest),
        ILLEGAL_FILE_UPDATE_REQUEST);
  }

  @Test
  @Transactional
  public void testFileUpdateWithPublishedAnalysis(){
    analysisService.securedUpdateState(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID, PUBLISHED);
    val originalAnalysis = analysisService.unsecuredDeepRead(DEFAULT_ANALYSIS_ID);
    assertThat(resolveAnalysisState(originalAnalysis.getAnalysisState())).isEqualTo(PUBLISHED);
    val originalFile = fileService.securedRead(DEFAULT_STUDY_ID, DEFAULT_FILE_ID);

    // No Change
    val noChangeRequest  = new FileUpdateRequest();
    val noChangeResponse =
        fileModificationService.securedFileWithAnalysisUpdate(DEFAULT_STUDY_ID, DEFAULT_FILE_ID, noChangeRequest);
    assertThat(noChangeResponse.isUnpublishedAnalysis()).isFalse();
    assertThat(noChangeResponse.getFileUpdateType()).isEqualTo(NO_UPDATE);
    assertThat(noChangeResponse.getOriginalAnalysisState()).isEqualTo(PUBLISHED);
    assertThat(noChangeResponse.getOriginalFile()).isEqualTo(originalFile);
    assertThat(noChangeResponse.getMessage())
        .isEqualTo("Original analysisState 'PUBLISHED' was not changed since the fileUpdateType was 'NO_UPDATE'");

    // Metadata Update
    val metadataUpdateRequest = FileUpdateRequest.builder()
        .info(object()
            .with(
                randomGenerator.generateRandomUUIDAsString(),
                randomGenerator.generateRandomUUIDAsString())
            .end())
        .build();
    val originalFile2 = fileService.securedRead(DEFAULT_STUDY_ID, DEFAULT_FILE_ID);
    val metadataUpdateResponse =
        fileModificationService.securedFileWithAnalysisUpdate(DEFAULT_STUDY_ID, DEFAULT_FILE_ID, metadataUpdateRequest);
    assertThat(metadataUpdateResponse.isUnpublishedAnalysis()).isFalse();
    assertThat(metadataUpdateResponse.getFileUpdateType()).isEqualTo(METADATA_UPDATE);
    assertThat(metadataUpdateResponse.getOriginalAnalysisState()).isEqualTo(PUBLISHED);
    assertThat(metadataUpdateResponse.getOriginalFile()).isEqualTo(originalFile2);
    assertThat(metadataUpdateResponse.getMessage())
        .isEqualTo("Original analysisState 'PUBLISHED' was not changed since the fileUpdateType was 'METADATA_UPDATE'");

    // Content Update
    val contentUpdateRequest = FileUpdateRequest.builder()
        .fileSize(originalFile2.getFileSize()+77771L)
        .build();
    val originalFile3 = fileService.securedRead(DEFAULT_STUDY_ID, DEFAULT_FILE_ID);
    val contentUpdateResponse =
        fileModificationService.securedFileWithAnalysisUpdate(DEFAULT_STUDY_ID, DEFAULT_FILE_ID, contentUpdateRequest);
    assertThat(contentUpdateResponse.isUnpublishedAnalysis()).isTrue();
    assertThat(contentUpdateResponse.getFileUpdateType()).isEqualTo(CONTENT_UPDATE);
    assertThat(contentUpdateResponse.getOriginalAnalysisState()).isEqualTo(PUBLISHED);
    assertThat(contentUpdateResponse.getOriginalFile()).isEqualTo(originalFile3);
    assertThat(contentUpdateResponse.getMessage())
        .isEqualTo("[WARNING]: Changed analysis from 'PUBLISHED' to 'UNPUBLISHED'");
  }

  @Test
  @Transactional
  public void testFileUpdateWithUnpublishedAnalysis(){
    val originalAnalysis = analysisService.unsecuredDeepRead(DEFAULT_ANALYSIS_ID);
    assertThat(resolveAnalysisState(originalAnalysis.getAnalysisState())).isEqualTo(UNPUBLISHED);
    val originalFile = fileService.securedRead(DEFAULT_STUDY_ID, DEFAULT_FILE_ID);

    // No Change
    val noChangeRequest  = new FileUpdateRequest();
    val noChangeResponse =
        fileModificationService.securedFileWithAnalysisUpdate(DEFAULT_STUDY_ID, DEFAULT_FILE_ID, noChangeRequest);
    assertThat(noChangeResponse.isUnpublishedAnalysis()).isFalse();
    assertThat(noChangeResponse.getFileUpdateType()).isEqualTo(NO_UPDATE);
    assertThat(noChangeResponse.getOriginalAnalysisState()).isEqualTo(UNPUBLISHED);
    assertThat(noChangeResponse.getOriginalFile()).isEqualTo(originalFile);
    assertThat(noChangeResponse.getMessage()).contains("Did not change analysisState since it is");

    // Metadata Update
    val metadataUpdateRequest = FileUpdateRequest.builder()
        .info(object()
            .with(
                randomGenerator.generateRandomUUIDAsString(),
                randomGenerator.generateRandomUUIDAsString())
            .end())
        .build();
    val originalFile2 = fileService.securedRead(DEFAULT_STUDY_ID, DEFAULT_FILE_ID);
    val metadataUpdateResponse =
        fileModificationService.securedFileWithAnalysisUpdate(DEFAULT_STUDY_ID, DEFAULT_FILE_ID, metadataUpdateRequest);
    assertThat(metadataUpdateResponse.isUnpublishedAnalysis()).isFalse();
    assertThat(metadataUpdateResponse.getFileUpdateType()).isEqualTo(METADATA_UPDATE);
    assertThat(metadataUpdateResponse.getOriginalAnalysisState()).isEqualTo(UNPUBLISHED);
    assertThat(metadataUpdateResponse.getOriginalFile()).isEqualTo(originalFile2);
    assertThat(metadataUpdateResponse.getMessage()).contains("Did not change analysisState since it is");

    // Content Update
    val contentUpdateRequest = FileUpdateRequest.builder()
        .fileSize(originalFile2.getFileSize()+77771L)
        .build();
    val originalFile3 = fileService.securedRead(DEFAULT_STUDY_ID, DEFAULT_FILE_ID);
    val contentUpdateResponse =
        fileModificationService.securedFileWithAnalysisUpdate(DEFAULT_STUDY_ID, DEFAULT_FILE_ID, contentUpdateRequest);
    assertThat(contentUpdateResponse.isUnpublishedAnalysis()).isFalse();
    assertThat(contentUpdateResponse.getFileUpdateType()).isEqualTo(CONTENT_UPDATE);
    assertThat(contentUpdateResponse.getOriginalAnalysisState()).isEqualTo(UNPUBLISHED);
    assertThat(contentUpdateResponse.getOriginalFile()).isEqualTo(originalFile3);
    assertThat(contentUpdateResponse.getMessage()).contains("Did not change analysisState since it is");
  }

  @Test
  public void testCheckFileUpdateRequestValidation(){
    val badSize_1  = FileUpdateRequest.builder().fileSize(-1L).build();
    val badSize_2  = FileUpdateRequest.builder().fileSize(0L).build();
    val badMd5_1 = FileUpdateRequest.builder().fileMd5sum("q123").build(); // less than 32 and non-hex number
    val badMd5_2 = FileUpdateRequest.builder().fileMd5sum("q0123456789012345678901234567890123456789").build(); //more than 32 and non-hex number
    val badAccess = FileUpdateRequest.builder().fileAccess("not_open_or_controlled").build();
    val badAll = FileUpdateRequest.builder()
        .fileAccess("not_open_or_controlled")
        .fileMd5sum("q123")
        .fileSize(0L)
        .info(object().with("something1", "value2").end())
        .build();

    val good = FileUpdateRequest.builder()
        .fileAccess(randomGenerator.randomEnum(AccessTypes.class).toString())
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .info(object().with("someKey", "someValue").end())
        .build();
    val good2 = FileUpdateRequest.builder()
        .fileAccess(randomGenerator.randomEnum(AccessTypes.class).toString())
        .build();
    val good3 = FileUpdateRequest.builder()
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .build();
    val good4 = FileUpdateRequest.builder()
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .build();
    val good5 = FileUpdateRequest.builder()
        .info(object().with("someKey", "someValue").end())
        .build();

    val goodRequests = newArrayList(good, good2, good3, good4, good5);
    val badRequests = newArrayList(badAccess, badMd5_1, badMd5_2, badSize_1, badSize_2, badAll);

    goodRequests.forEach(x -> fileModificationService.checkFileUpdateRequestValidation(DEFAULT_FILE_ID, x));

    for (val badRequest : badRequests){
      log.info("Processing bad request: {}", badRequest);
      assertSongError(
          () -> fileModificationService.checkFileUpdateRequestValidation(DEFAULT_FILE_ID, badRequest),
          INVALID_FILE_UPDATE_REQUEST, "Bad Request did not cause an error: %s" , badRequest);

      assertSongError(
          () -> fileModificationService.securedFileWithAnalysisUpdate(DEFAULT_STUDY_ID, DEFAULT_FILE_ID, badRequest),
          INVALID_FILE_UPDATE_REQUEST, "Bad Request did not cause an error: %s" , badRequest);
    }
  }

  @Test
  @Transactional
  public void testUpdateWithRequests(){
    val converter = Mappers.getMapper(FileConverter.class);
    val referenceFile = buildReferenceFile();
    val objectId = fileService.save(DEFAULT_ANALYSIS_ID, DEFAULT_STUDY_ID, referenceFile);
    referenceFile.setObjectId(objectId);
    val goldenFile = converter.copyFile(referenceFile);

    val u1 = FileUpdateRequest.builder()
        .fileAccess("controlled")
        .build();
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(METADATA_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setInfo(object().with(
        randomGenerator.generateRandomUUIDAsString(),
        randomGenerator.generateRandomUUIDAsString()).end());
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(METADATA_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileAccess("open");
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(METADATA_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileAccess(null);
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(METADATA_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileSize(19191L);
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileMd5sum(randomGenerator.generateRandomMD5());
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setInfo(null);
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileAccess(null);
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileMd5sum(uniqueMd5);
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileMd5sum(null);
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileSize(referenceFile.getFileSize());
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(NO_UPDATE);
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileSize(null);
    assertThat(fileModificationService.updateWithRequest(referenceFile, u1 )).isEqualTo(NO_UPDATE);
    assertThat(u1.getFileAccess()).isNull();
    assertThat(u1.getFileSize()).isNull();
    assertThat(u1.getFileMd5sum()).isNull();
    assertThat(u1.getInfo()).isNull();
    assertThat(referenceFile == goldenFile).isFalse();
    assertThat(referenceFile).isEqualTo(goldenFile);

  }
  @Test
  public void testFileUpdateTypeResolution(){
    // golden used to ensure f1 is not mutated
    val golden = buildReferenceFile();
    val f1 = buildReferenceFile();

    // update access field
    val u1 = FileUpdateRequest.builder()
        .fileAccess("controlled")
        .build();
    assertThat(resolveFileUpdateType(f1, u1)).isEqualTo(METADATA_UPDATE);

    // update info field
    u1.setInfo(object().with("myInfoKey2", "myInfoValue2").end());
    assertThat(resolveFileUpdateType(f1, u1)).isEqualTo(METADATA_UPDATE);

    // update file size
    val u2 = FileUpdateRequest.builder()
        .fileSize(123123L)
        .build();
    u1.setFileSize(123456L);
    // test request u1 with metadata updates
    assertThat(resolveFileUpdateType(f1, u1)).isEqualTo(CONTENT_UPDATE);
    // test request u2 without any metadata updates
    assertThat(resolveFileUpdateType(f1, u2)).isEqualTo(CONTENT_UPDATE);

    // update file md5
    u2.setFileMd5sum(randomGenerator.generateRandomMD5());
    u1.setFileMd5sum(randomGenerator.generateRandomMD5());
    // test request u1 with metadata updates
    assertThat(resolveFileUpdateType(f1, u1)).isEqualTo(CONTENT_UPDATE);
    // test request u2 without any metadata updates
    assertThat(resolveFileUpdateType(f1, u2)).isEqualTo(CONTENT_UPDATE);

    // test nulls
    val u3 = FileUpdateRequest.builder().build();
    assertThat(resolveFileUpdateType(f1, u3)).isEqualTo(NO_UPDATE);
    u3.setFileMd5sum(f1.getFileMd5sum());
    u3.setFileSize(f1.getFileSize());
    u3.setFileAccess(f1.getFileAccess());
    u3.setInfo(f1.getInfo());
    assertThat(resolveFileUpdateType(f1, u3)).isEqualTo(NO_UPDATE);

    assertThat(f1).isEqualTo(golden);
  }

  private FileEntity buildReferenceFile(){
    val referenceFile = FileEntity.builder()
        .analysisId("AN1")
        .objectId("FI1")
        .studyId("ABC123")
        .fileName("myFilename.bam")
        .fileAccess("open")
        .fileMd5sum(uniqueMd5)
        .fileSize(777777L)
        .fileType("BAM")
        .build();
    referenceFile.setInfo(object().with("myInfoKey1", "myInfoValue1").end());
    return referenceFile;
  }


}
