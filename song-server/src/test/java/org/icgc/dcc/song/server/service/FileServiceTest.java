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
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.FileEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.core.model.enums.AccessTypes.CONTROLLED;
import static org.icgc.dcc.song.core.model.enums.AccessTypes.OPEN;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_ANALYSIS_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_FILE_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.securestudy.impl.SecureFileTester.createSecureFileTester;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class FileServiceTest {


  @Autowired
  FileService fileService;
  @Autowired
  StudyService studyService;
  @Autowired
  AnalysisService analysisService;

  private final RandomGenerator randomGenerator = createRandomGenerator(FileServiceTest.class.getSimpleName());
  private final String UNIQUE_MD6_1 = randomGenerator.generateRandomMD5();

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
  }

  @Test
  public void testReadFile() {
    val id = DEFAULT_FILE_ID;
    val name = "ABC-TC285G7-A5-ae3458712345.bam";
    val analysisId="AN1";
    val study = DEFAULT_STUDY_ID;
    val type = "BAM";
    val size = 122333444455555L;
    val md5 = "20de2982390c60e33452bf8736c3a9f1";
    val access = OPEN;
    val metadata = JsonUtils.fromSingleQuoted("{'info':'<XML>Not even well-formed <XML></XML>'}");
    val file = fileService.securedRead(study, id);

    val expected = FileEntity.builder()
        .objectId(id)
        .analysisId(analysisId)
        .fileName(name)
        .studyId(study)
        .fileSize(size)
        .fileType(type)
        .fileMd5sum(md5)
        .fileAccess(access.toString())
        .build();
    expected.setInfo("name", "file1");
    assertThat(file).isEqualToComparingFieldByField(expected);
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


    val status = fileService.create(analysisId,  studyId, f);
    val id = f.getObjectId();

    assertThat(status).isEqualTo(id);

    FileEntity check = fileService.securedRead(studyId, id);
    assertThat(check).isEqualToComparingFieldByField(f);

    fileService.securedDelete(studyId, id);
    assertThat(fileService.isFileExist(id)).isFalse();
  }

  @Test
  public void testSaveFile(){
    val analysisId = DEFAULT_ANALYSIS_ID;
    val studyId = DEFAULT_STUDY_ID;
    analysisService.checkAnalysisExists(analysisId);

    val randomFile = createRandomFile(studyId, analysisId);
    val fileId = fileService.save(analysisId, studyId, randomFile);
    val actualFile = fileService.securedRead(studyId, fileId);
    assertThat(actualFile).isEqualToComparingFieldByFieldRecursively(randomFile);

    actualFile.setFileSize(1010101L);
    assertThat(actualFile).isNotEqualTo(randomFile);

    val updatedFileId = fileService.save(analysisId, studyId, actualFile);
    val updatedFile = fileService.securedRead(studyId, updatedFileId);
    assertThat(updatedFile).isEqualToComparingFieldByFieldRecursively(actualFile);
  }

  @Test
  public void testUpdateFile() {

    val study=DEFAULT_STUDY_ID;
    val id = "";
    val analysisId= DEFAULT_ANALYSIS_ID;
    val name = "file123.fasta";
    val sampleId = "";
    val size = 12345L;
    val type = "FASTA";
    val md5 = "md5sumaebcefghadwa";
    val access = CONTROLLED;
    val metadata = JsonUtils.fromSingleQuoted("'language': 'English'");

    val s = FileEntity.builder()
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

    val s2 = FileEntity.builder()
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
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

  @Test
  public void testFileExists(){
    val existingFileId= DEFAULT_FILE_ID;
    assertThat(fileService.isFileExist(existingFileId)).isTrue();
    fileService.checkFileExists(existingFileId);
    val file = new FileEntity();
    file.setObjectId(existingFileId);
    fileService.checkFileExists(file);

    val randomFile = createRandomFile(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID);
    assertThat(fileService.isFileExist(randomFile.getObjectId())).isFalse();
    assertSongError(() -> fileService.checkFileExists(randomFile.getObjectId()), FILE_NOT_FOUND);
    assertSongError(() -> fileService.checkFileExists(randomFile), FILE_NOT_FOUND);
  }

  @Test
  public void testStudyDNE(){
    val existingAnalysisId = DEFAULT_ANALYSIS_ID;
    analysisService.checkAnalysisExists(existingAnalysisId);

    val nonExistentStudyId = randomGenerator.generateRandomUUID().toString();
    val randomFile = createRandomFile(nonExistentStudyId, existingAnalysisId);
    assertSongError(
        () -> fileService.create(existingAnalysisId, nonExistentStudyId, randomFile),
        STUDY_ID_DOES_NOT_EXIST
    );
    assertSongError(
        () -> fileService.save(existingAnalysisId, nonExistentStudyId, randomFile),
        STUDY_ID_DOES_NOT_EXIST
    );
  }

  @Test
  public void testFileDNE(){
    val studyId = DEFAULT_STUDY_ID;
    val existingAnalysisId = DEFAULT_ANALYSIS_ID;
    analysisService.checkAnalysisExists(existingAnalysisId);

    val randomFile = createRandomFile(studyId, existingAnalysisId);
    assertSongError(() -> fileService.securedDelete(DEFAULT_STUDY_ID, randomFile.getObjectId()), FILE_NOT_FOUND );
  }

  @Test
  @Transactional
  public void testCheckFileUnrelatedToStudy(){
    val secureFileTester = createSecureFileTester(randomGenerator, studyService, fileService, analysisService);
    secureFileTester.runSecureTest((s,f) -> fileService.checkFileAndStudyRelated(s, f));
    secureFileTester.runSecureTest((s,f) -> fileService.securedRead(s, f));
    secureFileTester.runSecureTest((s,f) -> fileService.securedDelete(s, f));
    secureFileTester.runSecureTest((s,f) -> fileService.securedDelete(s, newArrayList(f)));
  }

  private FileEntity createRandomFile(String studyId, String analysisId){
    return FileEntity.builder()
        .objectId( randomGenerator.generateRandomUUIDAsString())
        .analysisId(analysisId)
        .fileName(randomGenerator.generateRandomUUIDAsString()+".bam")
        .studyId(studyId)
        .fileSize((long)randomGenerator.generateRandomInt())
        .fileType("BAM")
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .fileAccess(CONTROLLED.toString())
        .build();
  }


}
