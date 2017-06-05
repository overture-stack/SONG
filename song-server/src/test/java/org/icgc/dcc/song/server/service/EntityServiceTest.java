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

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import lombok.SneakyThrows;
import lombok.val;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class EntityServiceTest {

  // class under test
  @Autowired
  EntityService service;

  // trusted services used to verify class under test
  @Autowired
  DonorService donorService;
  @Autowired
  SpecimenService specimenService;
  @Autowired
  SampleService sampleService;
  @Autowired
  FileService fileService;

  @SneakyThrows
  @Test
  public void testSaveDonor_Create() {
    val studyId = "ABC123";
    val donor =
        JsonNodeFactory.instance.objectNode().put("donorSubmitterId", "donor_abc123").put("donorGender", "female");

    // This should create a new record, because there is no entry
    // in the existing test data for "donor_abc123"
    val id = service.saveDonor(studyId, donor);

    val d = donorService.read(id);
    assertThat(d.getDonorGender().equals("female"));
    assertThat(d.getDonorSubmitterId().equals("donor_abc123"));
    assertThat(d.getDonorId().equals(id));
  }

  @SneakyThrows
  @Test
  public void testSaveDonor_Update() {
    val studyId = "ABC123";

    val donor =
        JsonNodeFactory.instance.objectNode().put("donorSubmitterId", "Subject-X23Alpha7").put("donorGender", "female");

    // this submitterId and study already exists in our test data,
    // so we should update the existing donor record, not create a new one...
    val id = service.saveDonor(studyId, donor);

    // check that what we saved is what's in the database...
    val d = donorService.read(id);
    assertThat(d.getDonorGender().equals("female"));
    assertThat(d.getDonorSubmitterId().equals("Subject-X23Alpha7"));
    assertThat(d.getDonorId().equals(id));

    // make sure we're updating the donorID of the existing record in our test data ("DO1")
    assertThat(id.equals("DO1"));
  }

  @SneakyThrows
  @Test
  public void testSaveSpecimen_Create() {
    val studyId = "ABC123";

    val specimen = JsonNodeFactory.instance.objectNode().put("specimenSubmitterId", "specimen_abc123")
        .put("specimenClass", "Normal").put("specimenType", "Normal - solid tissue");

    // This should create a new record
    val donorId = "DO1";

    val id = service.saveSpecimen(studyId, donorId, specimen);
    val sp = specimenService.read(id);
    Assertions.assertThat(sp.getSpecimenId().equals(id));
    Assertions.assertThat(sp.getSpecimenClass()).isEqualTo("Normal");
    Assertions.assertThat(sp.getSpecimenType()).isEqualTo("Normal - solid tissue");
  }

  @SneakyThrows
  @Test
  public void testSaveSpecimen_Update() {
    val studyId = "ABC123";

    val specimen = JsonNodeFactory.instance.objectNode().put("specimenSubmitterId", "Tissue-Culture 284 Gamma 3")
        .put("specimenClass", "Tumour").put("specimenType", "Recurrent tumour - other");

    // This should update the record for specimen "SP1"
    val donorId = "DO1";

    val id = service.saveSpecimen(studyId, donorId, specimen);
    val sp = specimenService.read(id);
    assertThat(id.equals("SP1"));
    Assertions.assertThat(sp.getSpecimenId().equals(id));
    Assertions.assertThat(sp.getSpecimenSubmitterId().equals("Tissue-Culture 284 Gamma 3"));
    Assertions.assertThat(sp.getSpecimenClass().equals("Tumour"));
    Assertions.assertThat(sp.getSpecimenType().equals("Recurrent tumour - other"));
  }

  @SneakyThrows
  @Test
  public void testSaveSample_Create() {
    val studyId = "ABC123";

    val sample =
        JsonNodeFactory.instance.objectNode().put("sampleSubmitterId", "sample_abc123").put("sampleType", "Total RNA");

    // This should create a new record
    val specimenId = "SP1";

    val id = service.saveSample(studyId, specimenId, sample);
    val sa = sampleService.read(id);
    assertThat(sa.getSampleId()).isEqualTo(id);
    assertThat(sa.getSampleType()).isEqualTo("Total RNA");
    assertThat(sa.getSampleSubmitterId()).isEqualTo("sample_abc123");
  }

  @SneakyThrows
  @Test
  public void testSaveSample_Update() {
    val studyId = "ABC123";

    val sample =
        JsonNodeFactory.instance.objectNode().put("sampleSubmitterId", "T285-G7-B9").put("sampleType", "Total RNA");

    // This should update sample "SA11"
    val specimenId = "SP1";

    val id = service.saveSample(studyId, specimenId, sample);
    val sa = sampleService.read(id);
    assertThat(id.equals("SA11"));
    assertThat(sa.getSampleId().equals(id));
    assertThat(sa.getSampleType().equals("Total RNA"));
    assertThat(sa.getSampleSubmitterId().equals("sample_abc123"));
  }

  @SneakyThrows
  @Test
  public void testSaveFile_Create() {
    val studyId = "ABC123";

    val name = "file_abc123.idx.gz";
    val md5 = "mmmmdddd5555";
    val file = JsonNodeFactory.instance.objectNode().put("fileName", name).put("fileSize", 12345L).put("fileMd5", md5)
        .put("fileType", "IDX");

    // This should create a new record
    val sampleId = "SA1";

    val id = service.saveFile(studyId, sampleId, file);
    val f = fileService.read(id);
    assertThat(f.getObjectId().equals(id));
    assertThat(f.getFileName().equals(name));
    assertThat(f.getFileSize() == 12345L);
    assertThat(f.getFileMd5().equals(md5));
    assertThat(f.getFileType().equals("IDX"));
  }

  @SneakyThrows
  @Test
  public void testSaveFile_Update() {
    val studyId = "ABC123";

    val name = "ABC-TC285-G7-B9-kthx12345.bai";
    val md5 = "mmmmdddd5555";
    val file = JsonNodeFactory.instance.objectNode().put("fileName", name).put("fileSize", 12345L).put("fileMd5", md5)
        .put("fileType", "IDX");

    // This should update the row with ObjectId "FI3".
    val sampleId = "SA11";

    val id = service.saveFile(studyId, sampleId, file);
    val f = fileService.read(id);
    assertThat(id.equals("FI3"));
    assertThat(f.getObjectId().equals(id));
    assertThat(f.getFileName().equals(name));
    assertThat(f.getFileSize() == 12345L);
    assertThat(f.getFileMd5().equals(md5));
    assertThat(f.getFileType().equals("IDX"));
  }

}
