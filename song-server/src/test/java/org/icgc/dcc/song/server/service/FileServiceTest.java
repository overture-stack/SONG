/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.service;

import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.CONTROLLED;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.OPEN;
import static org.icgc.dcc.song.server.service.AnalysisService.checkAnalysis;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_ANALYSIS_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_FILE_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class FileServiceTest {

  @Autowired
  FileService fileService;
  @Autowired
  StudyService studyService;
  @Autowired
  AnalysisRepository analysisRepository;

  private final RandomGenerator randomGenerator = createRandomGenerator(FileServiceTest.class.getSimpleName());

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
    val file = fileService.read(id);

    val expected = File.create(id, analysisId, name, study, size, type, md5, access);
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

    File check = fileService.read(id);
    assertThat(check).isEqualToComparingFieldByField(f);

    fileService.delete(id);
    assertThat(fileService.isFileExist(id)).isFalse();
  }

  @Test
  public void testSaveFile(){
    val analysisId = DEFAULT_ANALYSIS_ID;
    val studyId = DEFAULT_STUDY_ID;
    checkAnalysis(analysisRepository, analysisId);
    studyService.checkStudyExist(studyId);

    val randomFile = createRandomFile(studyId, analysisId);
    val fileId = fileService.save(analysisId, studyId, randomFile);
    val actualFile = fileService.read(fileId);
    assertThat(actualFile).isEqualToComparingFieldByFieldRecursively(randomFile);

    actualFile.setFileSize(1010101L);
    assertThat(actualFile).isNotEqualTo(randomFile);

    val updatedFileId = fileService.save(analysisId, studyId, actualFile);
    val updatedFile = fileService.read(updatedFileId);
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

    val s = File.create(id, analysisId, name, sampleId, size, type, md5, access);


    fileService.create(analysisId, study, s);
    val id2 = s.getObjectId();

    val s2 = File.create(id2,  analysisId,"File 102.fai", study, 123456789L, "FAI",
            "e1f2a096d90c2cb9e63338e41d805977", CONTROLLED);
    s2.setInfo(metadata);
    fileService.update(s2);

    val s3 = fileService.read(id2);
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

  @Test
  public void testStudyDNE(){
    val existingAnalysisId = DEFAULT_ANALYSIS_ID;
    checkAnalysis(analysisRepository, existingAnalysisId);

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
    val existingAnalysisId = DEFAULT_ANALYSIS_ID;
    checkAnalysis(analysisRepository, existingAnalysisId);

    val studyId = DEFAULT_STUDY_ID;
    studyService.checkStudyExist(studyId);

    val randomFile = createRandomFile(studyId, existingAnalysisId);
    assertSongError(() -> fileService.update(randomFile), FILE_NOT_FOUND );
    assertSongError(() -> fileService.delete(randomFile.getObjectId()), FILE_NOT_FOUND );
  }

  private File createRandomFile(String studyId, String analysisId){
    return File.create(
        randomGenerator.generateRandomUUID().toString(),
        analysisId,
        randomGenerator.generateRandomUUID().toString()+".bam",
        studyId,
        (long)randomGenerator.generateRandomInt(),
        "BAM",
        randomGenerator.generateRandomMD5(),
        AccessTypes.CONTROLLED);
  }

}
