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
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class FileServiceTest {

  @Autowired
  FileService fileService;

  @Test
  public void testReadFile() {
    val id = "FI1";
    val name = "ABC-TC285G7-A5-ae3458712345.bam";
    val analysis_id="AN1";
    val study = "ABC123";
    val type = "BAM";
    val size = 122333444455555L;
    val md5 = "20de2982390c60e33452bf8736c3a9f1";
    val metadata = JsonUtils.fromSingleQuoted("{'info':'<XML>Not even well-formed <XML></XML>'}");
    val file = fileService.read(id);

    val expected = File.create(id, analysis_id, name, study, size, type, md5, metadata);
    assertThat(file).isEqualToComparingFieldByField(expected);
  }

  @Test
  public void testCreateAndDeleteFile() {
    val sampleId = "";
    val studyId="ABC123";
    val f = new File();

    f.setObjectId("");
    f.setFileName("ABC-TC285G87-A5-sqrl.bai");

    f.setStudyId(studyId);

    f.setFileSize(0L);
    f.setFileType("FAI");
    f.setFileMd5sum("md5abcdefg");

    val status = fileService.create("AN1",  studyId, f);
    val id = f.getObjectId();

    assertThat(status).isEqualTo(id);

    File check = fileService.read(id);
    assertThat(f).isEqualToComparingFieldByField(check);

    fileService.delete(id);
    val check2 = fileService.read(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateFile() {

    val study="ABC123";
    val id = "";
    val analysis_id="AN1";
    val name = "file123.fasta";
    val sampleId = "";
    val size = 12345L;
    val type = "FASTA";
    val md5 = "md5sumaebcefghadwa";
    val metadata = "";

    val s = File.create(id, analysis_id,name, sampleId, size, type, md5, metadata);

    fileService.create("AN1", study, s);
    val id2 = s.getObjectId();

    val s2 = File.create(id2,  analysis_id,"File 102.fai", study, 123456789L, "FAI", "md5magical", "");
    fileService.update(s2);

    val s3 = fileService.read(id2);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
