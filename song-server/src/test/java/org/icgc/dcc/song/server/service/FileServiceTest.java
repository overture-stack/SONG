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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.converter.FileConverter;
import org.icgc.dcc.song.server.model.entity.file.File;
import org.icgc.dcc.song.server.model.entity.file.FileUpdateRequest;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.entity.file.FileUpdateRequest.createFileUpdateRequest;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.CONTROLLED;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.OPEN;
import static org.icgc.dcc.song.server.model.enums.FileUpdateTypes.CONTENT_UPDATE;
import static org.icgc.dcc.song.server.model.enums.FileUpdateTypes.METADATA_UPDATE;
import static org.icgc.dcc.song.server.model.enums.FileUpdateTypes.NO_UPDATE;
import static org.icgc.dcc.song.server.model.enums.FileUpdateTypes.resolveFileUpdateType;
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
  private final String UNIQUE_MD5_1 = randomGenerator.generateRandomMD5();

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

    val expected = File.builder()
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
    val f = new File();

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

    File check = fileService.securedRead(studyId, id);
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

    val s = File.builder()
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

    val s2 = File.builder()
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
    fileService.securedUpdate(study, id2, s2);

    val s3 = fileService.securedRead(study, id2);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

  @Test
  public void testFileExists(){
    val existingFileId= DEFAULT_FILE_ID;
    assertThat(fileService.isFileExist(existingFileId)).isTrue();
    fileService.checkFileExists(existingFileId);
    val file = new File();
    file.setObjectId(existingFileId);
    fileService.checkFileExists(file);

    val randomFile = createRandomFile(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID);
    assertThat(fileService.isFileExist(randomFile.getObjectId())).isFalse();
    assertSongError(() -> fileService.checkFileExists(randomFile.getObjectId()), FILE_NOT_FOUND);
    assertSongError(() -> fileService.checkFileExists(randomFile), FILE_NOT_FOUND);
  }

  private File buildReferenceFile(){
    val referenceFile = File.builder()
        .analysisId("AN1")
        .objectId("FI1")
        .studyId("ABC123")
        .fileName("myFilename.bam")
        .fileAccess("open")
        .fileMd5sum(UNIQUE_MD5_1)
        .fileSize(777777L)
        .fileType("BAM")
        .build();
    referenceFile.setInfo(object().with("myInfoKey1", "myInfoValue1").end());
    return referenceFile;
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

  @Test
  @SneakyThrows
  @Transactional
  public void testCreateUpdateFile(){
    val ref = buildReferenceFile();
    val objectId = fileService.save(DEFAULT_ANALYSIS_ID, DEFAULT_STUDY_ID, ref);
    ref.setObjectId(objectId);

    val updateRequest = FileUpdateRequest.builder().build();

    val outFile = fileService.createUpdateFile( ref,  updateRequest );
    assertThat(outFile == ref).isFalse();
    assertThat(outFile).isEqualTo(ref);

    updateRequest.setFileSize(2348982L);
    val outFile2 = fileService.createUpdateFile( ref,  updateRequest );
    assertThat(outFile2 == ref).isFalse();
    assertThat(outFile2).isNotEqualTo(ref);
  }

  @Test
  @Transactional
  public void testUpdateAndSave(){
    val converter = Mappers.getMapper(FileConverter.class);
    val referenceFile = buildReferenceFile();
    val objectId = fileService.save(DEFAULT_ANALYSIS_ID, DEFAULT_STUDY_ID, referenceFile);
    referenceFile.setObjectId(objectId);
    val goldenFile = converter.copyFile(referenceFile);

    val u1 = FileUpdateRequest.builder()
        .fileAccess("controlled")
        .build();
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(METADATA_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setInfo(object().with(
        randomGenerator.generateRandomUUIDAsString(),
        randomGenerator.generateRandomUUIDAsString()).end());
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(METADATA_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileAccess("open");
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(METADATA_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileAccess(null);
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(METADATA_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileSize(19191L);
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileMd5sum(randomGenerator.generateRandomMD5());
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setInfo(null);
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileAccess(null);
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileMd5sum(UNIQUE_MD5_1);
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileMd5sum(null);
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(CONTENT_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileSize(referenceFile.getFileSize());
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(NO_UPDATE);
    assertThat(referenceFile).isEqualTo(goldenFile);

    u1.setFileSize(null);
    assertThat(fileService.updateAndSave(referenceFile, u1 )).isEqualTo(NO_UPDATE);
    assertThat(u1.getFileAccess()).isNull();
    assertThat(u1.getFileSize()).isNull();
    assertThat(u1.getFileMd5sum()).isNull();
    assertThat(u1.getInfo()).isNull();
    assertThat(referenceFile).isEqualTo(goldenFile);

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
    assertSongError(() -> fileService.securedUpdate(studyId, randomFile.getObjectId(), randomFile), FILE_NOT_FOUND );
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

    val randomFileUpdateRequest = createFileUpdateRequest(
        (long)randomGenerator.generateRandomIntRange(1,100000),
        randomGenerator.generateRandomMD5(),
        randomGenerator.randomEnum(AccessTypes.class).toString(),
        object().end());
    secureFileTester.runSecureTest((s,f) -> fileService.securedUpdate(s, f, randomFileUpdateRequest));
  }

  private File createRandomFile(String studyId, String analysisId){
    return File.builder()
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
