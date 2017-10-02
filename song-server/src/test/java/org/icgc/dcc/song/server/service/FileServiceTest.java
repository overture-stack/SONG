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
import org.icgc.dcc.song.server.model.entity.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.CONTROLLED;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.OPEN;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
//@FlywayTest
@ActiveProfiles("dev")
public class FileServiceTest {

  @Autowired
  FileService fileService;

  @Test
  public void testReadFile() {
    val id = "FI1";
    val name = "ABC-TC285G7-A5-ae3458712345.bam";
    val analysisId="AN1";
    val study = "ABC123";
    val type = "BAM";
    val size = 122333444455555L;
    val md5 = "20de2982390c60e33452bf8736c3a9f1";
    val access = OPEN;
    val metadata = JsonUtils.fromSingleQuoted("{'info':'<XML>Not even well-formed <XML></XML>'}");
    val file = fileService.read(id);

    val expected = File.create(id, analysisId, name, study, size, type, md5, access);
    assertThat(file).isEqualToComparingFieldByField(expected);
  }

  @Test
  public void testCreateAndDeleteFile() {
    val studyId="ABC123";
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


    val status = fileService.create("AN1",  studyId, f);
    val id = f.getObjectId();

    assertThat(status).isEqualTo(id);

    File check = fileService.read(id);
    assertThat(check).isEqualToComparingFieldByField(f);

    fileService.delete(id);
    val check2 = fileService.read(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateFile() {

    val study="ABC123";
    val id = "";
    val analysisId="AN1";
    val name = "file123.fasta";
    val sampleId = "";
    val size = 12345L;
    val type = "FASTA";
    val md5 = "md5sumaebcefghadwa";
    val access = CONTROLLED;
    val metadata = JsonUtils.fromSingleQuoted("'language': 'English'");

    val s = File.create(id, analysisId,name, sampleId, size, type, md5, access);


    fileService.create("AN1", study, s);
    val id2 = s.getObjectId();

    val s2 = File.create(id2,  analysisId,"File 102.fai", study, 123456789L, "FAI",
            "e1f2a096d90c2cb9e63338e41d805977", CONTROLLED);
    s2.setInfo(metadata);
    fileService.update(s2);

    val s3 = fileService.read(id2);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
