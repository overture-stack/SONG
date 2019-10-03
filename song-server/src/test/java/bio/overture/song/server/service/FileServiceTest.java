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

import static bio.overture.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.model.enums.AccessTypes.CONTROLLED;
import static bio.overture.song.core.model.enums.AccessTypes.OPEN;
import static bio.overture.song.core.testing.SongErrorAssertions.assertExceptionThrownBy;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_ANALYSIS_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_FILE_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static bio.overture.song.server.utils.securestudy.impl.SecureFileTester.createSecureFileTester;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.FileEntity;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class FileServiceTest {

  @Autowired FileService fileService;
  @Autowired StudyService studyService;
  @Autowired AnalysisService analysisService;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(FileServiceTest.class.getSimpleName());

  @Before
  public void beforeTest() {
    assertTrue(studyService.isStudyExist(DEFAULT_STUDY_ID));
  }

  @Test
  public void testReadFile() {
    val id = DEFAULT_FILE_ID;
    val name = "ABC-TC285G7-A5-ae3458712345.bam";
    val analysisId = "AN1";
    val study = DEFAULT_STUDY_ID;
    val type = "BAM";
    val size = 122333444455555L;
    val md5 = "20de2982390c60e33452bf8736c3a9f1";
    val file = fileService.securedRead(study, id);

    val expected =
        FileEntity.builder()
            .objectId(id)
            .analysisId(analysisId)
            .fileName(name)
            .studyId(study)
            .fileSize(size)
            .fileType(type)
            .fileMd5sum(md5)
            .fileAccess(OPEN.toString())
            .build();
    expected.setInfo("name", "file1");
    assertEquals(file, expected);
  }

  @Test
  public void testCreateAndDeleteFile() {
    val studyId = DEFAULT_STUDY_ID;
    val analysisId = DEFAULT_ANALYSIS_ID;
    val metadata = JsonUtils.fromSingleQuoted("{'species': 'human'}");
    val f = new FileEntity();

    f.setObjectId("");
    f.setFileName("ABC-TC285G87-A5-sqrl.bai");

    f.setStudyId(studyId);

    f.setFileSize(0L);
    f.setFileType("FAI");
    f.setFileMd5sum("6bb8ee7218e96a59e0ad898b4f5360f1");
    f.setInfo(metadata);
    f.setFileAccess(OPEN);

    val status = fileService.create(analysisId, studyId, f);
    val id = f.getObjectId();

    assertEquals(status, id);

    FileEntity check = fileService.securedRead(studyId, id);
    assertEquals(check, f);

    fileService.securedDelete(studyId, id);
    assertFalse(fileService.isFileExist(id));
  }

  @Test
  public void testSaveFile() {
    val analysisId = DEFAULT_ANALYSIS_ID;
    val studyId = DEFAULT_STUDY_ID;
    analysisService.checkAnalysisExists(analysisId);

    val randomFile = createRandomFile(studyId, analysisId);
    val fileId = fileService.save(analysisId, studyId, randomFile);
    val actualFile = fileService.securedRead(studyId, fileId);
    assertEquals(actualFile, randomFile);

    actualFile.setFileSize(1010101L);
    assertNotEquals(actualFile, randomFile);

    val updatedFileId = fileService.save(analysisId, studyId, actualFile);
    val updatedFile = fileService.securedRead(studyId, updatedFileId);
    assertEquals(updatedFile, actualFile);
  }

  @Test
  public void testSaveFileAsTgz() {
    val analysisId = DEFAULT_ANALYSIS_ID;
    val studyId = DEFAULT_STUDY_ID;
    analysisService.checkAnalysisExists(analysisId);

    val randomFile = createRandomFileWithType(studyId, analysisId, "TGZ", ".tgz");
    val fileId = fileService.save(analysisId, studyId, randomFile);
    val actualFile = fileService.securedRead(studyId, fileId);
    assertEquals(actualFile, randomFile);

    actualFile.setFileSize(1010101L);
    assertNotEquals(actualFile, randomFile);

    val updatedFileId = fileService.save(analysisId, studyId, actualFile);
    val updatedFile = fileService.securedRead(studyId, updatedFileId);
    assertEquals(updatedFile, actualFile);
  }

  @Test
  public void testCreateFileUnknownType() {
    assertExceptionThrownBy(
        IllegalStateException.class,
        () ->
            FileEntity.builder()
                .fileAccess("controlled")
                .fileMd5sum(randomGenerator.generateRandomMD5())
                .fileName(randomGenerator.generateRandomAsciiString(10))
                .fileSize((long) randomGenerator.generateRandomInt(100, 100000))
                .analysisId(randomGenerator.generateRandomUUIDAsString())
                .objectId(randomGenerator.generateRandomUUIDAsString())
                .studyId(randomGenerator.generateRandomAsciiString(7))
                .fileType("TGZZZZZ")
                .build());
  }

  @Test
  public void testUpdateFile() {

    val study = DEFAULT_STUDY_ID;
    val id = "";
    val analysisId = DEFAULT_ANALYSIS_ID;
    val name = "file123.fasta";
    val sampleId = "";
    val size = 12345L;
    val type = "FASTA";
    val md5 = "md5sumaebcefghadwa";
    val access = CONTROLLED;
    val metadata = JsonUtils.fromSingleQuoted("'language': 'English'");

    val s =
        FileEntity.builder()
            .objectId(id)
            .analysisId(analysisId)
            .fileName(name)
            .studyId(study)
            .fileSize(size)
            .fileType(type)
            .fileMd5sum(md5)
            .fileAccess(access.toString())
            .build();

    fileService.create(analysisId, study, s);
    val id2 = s.getObjectId();

    val s2 =
        FileEntity.builder()
            .objectId(id2)
            .analysisId(analysisId)
            .fileName("File 102.fai")
            .studyId(study)
            .fileSize(123456789L)
            .fileType("FAI")
            .fileMd5sum("e1f2a096d90c2cb9e63338e41d805977")
            .fileAccess(CONTROLLED.toString())
            .build();
    s2.setInfo(metadata);
    fileService.unsafeUpdate(s2);

    val s3 = fileService.securedRead(study, id2);
    assertEquals(s3, s2);
  }

  @Test
  public void testFileExists() {
    val existingFileId = DEFAULT_FILE_ID;
    assertTrue(fileService.isFileExist(existingFileId));
    fileService.checkFileExists(existingFileId);
    val file = new FileEntity();
    file.setObjectId(existingFileId);
    fileService.checkFileExists(file);

    val randomFile = createRandomFile(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID);
    assertFalse(fileService.isFileExist(randomFile.getObjectId()));
    SongErrorAssertions.assertSongErrorRunnable(
        () -> fileService.checkFileExists(randomFile.getObjectId()), FILE_NOT_FOUND);
    SongErrorAssertions.assertSongErrorRunnable(
        () -> fileService.checkFileExists(randomFile), FILE_NOT_FOUND);
  }

  @Test
  public void testStudyDNE() {
    val existingAnalysisId = DEFAULT_ANALYSIS_ID;
    analysisService.checkAnalysisExists(existingAnalysisId);

    val nonExistentStudyId = randomGenerator.generateRandomUUID().toString();
    val randomFile = createRandomFile(nonExistentStudyId, existingAnalysisId);
    SongErrorAssertions.assertSongError(
        () -> fileService.create(existingAnalysisId, nonExistentStudyId, randomFile),
        STUDY_ID_DOES_NOT_EXIST);
    SongErrorAssertions.assertSongError(
        () -> fileService.save(existingAnalysisId, nonExistentStudyId, randomFile),
        STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testFileDNE() {
    val studyId = DEFAULT_STUDY_ID;
    val existingAnalysisId = DEFAULT_ANALYSIS_ID;
    analysisService.checkAnalysisExists(existingAnalysisId);

    val randomFile = createRandomFile(studyId, existingAnalysisId);
    assertSongError(
        () -> fileService.securedDelete(DEFAULT_STUDY_ID, randomFile.getObjectId()),
        FILE_NOT_FOUND);
  }

  @Test
  @Transactional
  public void testCheckFileUnrelatedToStudy() {
    val secureFileTester =
        createSecureFileTester(randomGenerator, studyService, fileService, analysisService);
    secureFileTester.runSecureTest((s, f) -> fileService.checkFileAndStudyRelated(s, f));
    secureFileTester.runSecureTest((s, f) -> fileService.securedRead(s, f));
    secureFileTester.runSecureTest((s, f) -> fileService.securedDelete(s, f));
    secureFileTester.runSecureTest((s, f) -> fileService.securedDelete(s, newArrayList(f)));
  }

  private FileEntity createRandomFile(String studyId, String analysisId) {
    return createRandomFileWithType(studyId, analysisId, "BAM", ".bam");
  }

  private FileEntity createRandomFileWithType(
      String studyId, String analysisId, String fileType, String extension) {
    return FileEntity.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .analysisId(analysisId)
        .fileName(randomGenerator.generateRandomUUIDAsString() + extension)
        .studyId(studyId)
        .fileSize((long) randomGenerator.generateRandomInt())
        .fileType(fileType)
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .fileAccess(CONTROLLED.toString())
        .build();
  }
}
